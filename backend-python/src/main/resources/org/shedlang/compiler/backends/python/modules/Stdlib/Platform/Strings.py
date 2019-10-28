import io

from .. import Options

def code_point_at(index, string):
    if index < len(string):
        return Options.some(string[index])
    else:
        return Options.none


def code_point_to_hex_string(char):
    return format(ord(char), "X")


def code_point_to_int(char):
    return ord(char)


def code_point_to_string(char):
    return char


def code_point_count(string):
    return len(string)


def flat_map_code_points(func, string):
    result = io.StringIO()
    for char in string:
        result.write(func(char))
    return result.getvalue()


def fold_left_code_points(func, initial, string):
    result = initial
    for char in string:
        result = func(result, char)
    return result


def repeat(string, times):
    return string * times


def replace(old, new, string):
    return string.replace(old, new)


def substring(start_index, end_index, value):
    return value[start_index:end_index]
