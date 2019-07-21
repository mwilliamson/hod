package org.shedlang.compiler.types

import org.shedlang.compiler.all
import org.shedlang.compiler.zip3

fun isSubEffect(subEffect: Effect, superEffect: Effect): Boolean {
    val solver = TypeConstraintSolver(parameters = setOf())
    return solver.coerceEffect(from = subEffect, to = superEffect)
}

fun canCoerce(from: Type, to: Type): Boolean {
    return coerce(from = from, to = to) is CoercionResult.Success
}

fun isEquivalentType(first: Type, second: Type): Boolean {
    return canCoerce(from = first, to = second) && canCoerce(from = second, to = first)
}

fun coerce(
    from: Type,
    to: Type,
    parameters: Set<TypeParameter> = setOf()
): CoercionResult {
    return coerce(listOf(from to to), parameters = parameters)
}

fun coerce(
    constraints: List<Pair<Type, Type>>,
    parameters: Set<TypeParameter> = setOf()
): CoercionResult {
    val solver = TypeConstraintSolver(parameters = parameters)
    for ((from, to) in constraints) {
        if (!solver.coerce(from = from, to = to)) {
            return CoercionResult.Failure
        }
    }
    return CoercionResult.Success(solver.typeBindings)
}

sealed class CoercionResult {
    class Success(val bindings: Map<TypeParameter, Type>): CoercionResult()
    object Failure: CoercionResult()
}

class TypeConstraintSolver(
    private val parameters: Set<StaticParameter>
) {
    val typeBindings: MutableMap<TypeParameter, Type> = mutableMapOf()
    val effectBindings: MutableMap<EffectParameter, Effect> = mutableMapOf()
    private val closed: MutableSet<TypeParameter> = mutableSetOf()

    fun boundTypeFor(parameter: TypeParameter): Type? {
        val boundType = typeBindings[parameter]
        return if (boundType != null) {
            boundType
        } else if (parameter.variance == Variance.COVARIANT) {
            NothingType
        } else if (parameter.variance == Variance.CONTRAVARIANT) {
            AnyType
        } else {
            null
        }
    }

    fun boundEffectFor(parameter: EffectParameter): Effect {
        return effectBindings[parameter] ?: EmptyEffect
    }

    fun coerce(from: Type, to: Type): Boolean {
        if (from == to || to == AnyType || from == NothingType) {
            return true
        }

        if (from is SymbolType && to == AnySymbolType) {
            return true
        }

        if (to is TypeParameter && to in parameters) {
            val boundType = typeBindings[to]
            if (boundType == null) {
                typeBindings[to] = from
                return true
            } else if (to in closed) {
                return coerce(from = from, to = boundType)
            } else {
                typeBindings[to] = union(boundType, from)
                return true
            }
        }

        if (from is TypeParameter && from in parameters) {
            val boundType = typeBindings[from]
            if (boundType == null) {
                typeBindings[from] = to
                closed.add(from)
                return true
            } else {
                return coerce(from = boundType, to = to)
            }
        }

        if (from is TypeAlias) {
            return coerce(from = from.aliasedType, to = to)
        }

        if (to is TypeAlias) {
            return coerce(from = from, to = to.aliasedType)
        }

        // TODO: deal with type parameters
        if (from is UnionType) {
            return from.members.all({ member -> coerce(from = member, to = to) })
        }

        if (to is UnionType) {
            return to.members.any({ member -> coerce(from = from, to = member) })
        }

        if (from is FunctionType && to is FunctionType) {
            return (
                from.staticParameters.isEmpty() && to.staticParameters.isEmpty() &&
                    from.positionalParameters.size == to.positionalParameters.size &&
                    from.positionalParameters.zip(to.positionalParameters, { fromArg, toArg -> coerce(from = toArg, to = fromArg) }).all() &&
                    from.namedParameters.keys == to.namedParameters.keys &&
                    from.namedParameters.all({ fromArg -> coerce(from = to.namedParameters[fromArg.key]!!, to = fromArg.value) }) &&
                    coerceEffect(from = from.effect, to = to.effect) &&
                    coerce(from = from.returns, to = to.returns)
                )
        }

        if (from is TupleType && to is TupleType) {
            return from.elementTypes.size == to.elementTypes.size &&
                from.elementTypes.zip(to.elementTypes).all { (fromElement, toElement) ->
                    canCoerce(from = fromElement, to = toElement)
                }
        }

        if (from is ShapeType && to is ShapeType) {
            return from.shapeId == to.shapeId && zip3(
                from.staticParameters,
                from.staticArguments,
                to.staticArguments,
                { parameter, fromArg, toArg ->
                    parameter.accept(object : StaticParameter.Visitor<Boolean> {
                        override fun visit(parameter: TypeParameter): Boolean {
                            return fromArg is Type && toArg is Type && when (parameter.variance) {
                                Variance.INVARIANT -> isEquivalentType(fromArg, toArg)
                                Variance.COVARIANT -> coerce(from = fromArg, to = toArg)
                                Variance.CONTRAVARIANT -> coerce(from = toArg, to = fromArg)
                            }
                        }

                        override fun visit(parameter: EffectParameter): Boolean {
                            return fromArg is Effect && toArg is Effect && coerceEffect(from = fromArg, to = toArg)
                        }
                    })
                }
            ).all()
        }

        return false
    }

    fun coerceEffect(from: Effect, to: Effect): Boolean {
        if (from == to) {
            return true
        }

        if (from == EmptyEffect) {
            return true
        }

        if (to is EffectParameter && to in parameters) {
            val boundEffect = effectBindings[to]
            if (boundEffect == null) {
                effectBindings[to] = from
                return true
            } else {
                return coerceEffect(from = from, to = boundEffect)
            }
        }

        if (from is EffectParameter && from in parameters) {
            val boundEffect = effectBindings[from]
            if (boundEffect == null) {
                effectBindings[from] = to
                return true
            } else {
                return coerceEffect(from = boundEffect, to = to)
            }
        }

        return false
    }

    private fun isEquivalentType(left: Type, right: Type): Boolean {
        return coerce(from = left, to = right) && coerce(from = right, to = left)
    }
}
