function intToString(value) {
    return value.toString();
}

function print(value) {
    process.stdout.write(value);
}

function list() {
    return Array.prototype.slice.call(arguments);
}

function all(list) {
    for (var index = 0; index < list.length; index++) {
        if (!list[index]) {
            return false;
        }
    }
    return true;
}

function map(func, list) {
    return list.map(func);
}

function forEach(func, list) {
    return list.forEach(func);
}

function reduce(func, initial, list) {
    return list.reduce(func, initial);
}

function declareShape(name, constantFields) {
    const typeId = freshTypeId();

    function shape(fields) {
        if (fields === undefined) {
            fields = {};
        }
        for (var key in constantFields) {
            fields[key] = constantFields[key];
        }
        fields.$shedType = shape;
        return fields;
    }

    shape.typeId = typeId;
    shape.typeName = name;

    return shape;
}

var nextTypeId = 1;
function freshTypeId() {
    return nextTypeId++;
}

function isType(value, type) {
    return value != null && value.$shedType === type;
}

function symbolFactory() {
    const symbols = new Map();
    return function(name) {
        if (!symbols.has(name)) {
            symbols.set(name, {});
        }
        return symbols.get(name);
    };
}

module.exports = {
    declareShape: declareShape,
    isType: isType,
    symbolFactory: symbolFactory,

    all: all,
    forEach: forEach,
    intToString: intToString,
    list: list,
    map: map,
    print: print,
    reduce: reduce,
};
