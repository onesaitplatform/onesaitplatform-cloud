/* Order must match model. */
var SchemaAttributes = {
    dollarschema: {
        ref: 'li.DollarSchema',
        name: '$schema'
    },
    dollarref: {
        ref: 'li.DollarRef',
        name: '$ref'
    },
    title: {
        ref: 'li.Title',
        name: 'title'
    },
    schemaid: {
        ref: 'li.SchemaId',
        name: 'id'
    },
    name: {
        ref: 'li.Name',
        name: 'name'
    },
    description: {
        ref: 'li.Description',
        name: 'description'
    },
    required: {
        ref: 'li.Required',
        name: 'required'
    },
    minimum: {
        ref: 'li.Minimum',
        name: 'minimum'
    },
    maximum: {
        ref: 'li.Maximum',
        name: 'maximum'
    },
    minitems: {
        ref: 'li.MinItems',
        name: 'minitems'
    },
    maxitems: {
        ref: 'li.MaxItems',
        name: 'maxitems'
    },
    defaultValue: {
        ref: 'li.DefaultValue',
        name: 'default'
    }
};


var Templates = {};

TypeEnum = {
    STRING: "string",
    NUMBER: "number",
    INTEGER: "integer",
    BOOLEAN: "boolean",
    OBJECT: "object",
    ARRAY: "array",
    NULL: "null",
    ANY: "any",
    UNDEFINED: "undefined"
};

ListTypeEnum = {
    PROPERTIES: "properties",
    ITEMS: "items"
};

function ProxyResult() {
    value: undefined;
}

function ProxyRequest(url, result) {
    $.ajax({
        url: 'proxy.php',
        data: {
            action: 'ref',
            url: url
        },
        type: 'post',
        dataType: 'json',
        global: false,
        async: false,
        success: function(output) {
            result['value'] = output;
        }
    });
}

function RealTypeOf(v) {
    if (typeof(v) == TypeEnum.OBJECT) {

        if (v === null) {
            return TypeEnum.NULL;
        }
        if (v.constructor == (new Array).constructor) {
            return TypeEnum.ARRAY;
        }
        return TypeEnum.OBJECT;
    }
    return typeof(v);
}
