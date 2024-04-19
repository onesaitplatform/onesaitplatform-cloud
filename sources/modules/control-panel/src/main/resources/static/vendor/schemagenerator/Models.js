// ------------------- Models ------------------- //
$(document).ready(function() {

    Type = Backbone.Model.extend({
        defaults: {
            t: undefined
        }
    });

    TypeList = Backbone.Collection.extend({
        model: Type,

        filterByType: function(aType) {
            return this.filter(

            function(item) {
                return item.get('t') == aType;
            });
        },

        isObject: function() {
            return (this.filterByType('object').length > 0);
        },

        isString: function() {
            return (this.filterByType('string').length > 0);
        },

        isNumber: function() {
            return (this.filterByType('number').length > 0);
        },

        isInteger: function() {
            return (this.filterByType('integer').length > 0);
        },

        isBoolean: function() {
            return (this.filterByType('boolean').length > 0);
        },

        isNull: function() {
            return (this.filterByType('null').length > 0);
        },

        isAny: function() {
            return (this.filterByType('any').length > 0);
        },

        isArray: function() {
            return (this.filterByType('array').length > 0);
        }
    });

    SchemaPair = Backbone.Model.extend({
        defaults: {
            key: undefined,
            schema: undefined,
            removed: false,
            root: false
        },

        constructId: function(aParentId, aPos, aType) {
            console.log(this.get('schema').get('schemaid'));
            if (this.get('schema').get('schemaid') != "")
                return;

            var id;

            if (this.get('root')) {
            	var comboColecciones=document.getElementById("collections");
            	if (comboColecciones != null) {
                	id = comboColecciones.options[comboColecciones.selectedIndex].value;
                } else {
                	id = "";
                }
            } else if (!this.get('key')) {
                // Items and Extensions don't have keys.
                if ('extensions' == aType) {

                } else if ('items' == aType) {
                    id = aParentId + "/" + aPos.toString();
                }

            } else {
                id = aParentId + "/" + this.get('key');
            }

            this.get('schema').setId(id);
        },

        validate: function(attrs) {

            if (typeof(attrs.key) != "undefined") {
                if (!attrs.key) {
                    var k = attrs.key.replace(/\s/g, '');
                    if (k == '') {
                        return -1;
                    }
                }
            }
        }
    });

    SchemaPairList = Backbone.Collection.extend({
        model: SchemaPair,

        initialize: function() {
            this.comparator = function(aSchemaPair) {

                return (aSchemaPair.get('key') + aSchemaPair.cid);
            };
        },

        filterByRemoved: function() {
            return this.filter(

            function(item) {
                return item.get('removed');
            });
        },

        filterByName: function(aName) {
            return this.filter(

            function(item) {
                return (item.get('key') == aName);
            });
        },
    });

    Schema = Backbone.Model.extend({

        defaults: {
            // Simple
            dollarschema: '',
            dollarref: '',
            title: '',
            name: '',
            description: '',
            minimum: '',
            maximum: '',
            minitems: '',
            maxitems: '',
            schemaid: '',
            defaultValue: '',
            required: false,
            // Complex
            items: undefined,
            type: undefined,
            properties: undefined,
            extensions: undefined
        },

        setId: function(aId) {
            this.set({
                schemaid: aId
            });

            for (var i = 0; i < this.get('properties').length; i++) {
                this.get('properties').at(i).constructId(aId, i, 'poperties');
            }

            for (var i = 0; i < this.get('items').length; i++) {
                this.get('items').at(i).constructId(aId, i, 'items');
            }

            for (var i = 0; i < this.get('extensions').length; i++) {
                this.get('extensions').at(i).constructId(aId, i, 'extensions');
            }
        },

        complexKeys: function() {
            return ['properties', 'type', 'items', 'extensions'];
        },

        /* Used when input is schema to treat schema attributes 
        appropriately. These names must match schema definition, 
        hence 'extends' and not 'extensions'. */
        complexSchemaKeys: function() {
            return ['properties', 'type', 'items', 'extends', '$ref'];
        },

        simpleKeys: function() {
            return _.difference(_.keys(this.attributes), this.complexKeys());
        },

        simpleAttributes: function() {
            return _.pick(this.attributes, this.simpleKeys());
        },

        complexAttributes: function() {
            return _.pick(this.attributes, this.complexKeys());
        },

        simpleKeysWithVal: function() {
            return _.keys(this.simpleAttributesWithVal());
        },

        simpleKeysWithValConcise: function() {
            return _.keys(this.simpleAttributesWithValConcise());

        },

        simpleAttributesWithVal: function() {

            var self = this;
            var keepAttributes = _.filter(this.simpleKeys(), function(key) {

                var value = self.attributes[key];
                var type = RealTypeOf(value);

                // '' is falsy, so need to handle booleans explicitly.
                if (TypeEnum.BOOLEAN == type) {
                    return true;
                }

                return (value != '' && 
                        type != TypeEnum.UNDEFINED &&
                        type != TypeEnum.NULL);
                
            });

            return _.pick(this.attributes, keepAttributes);
        },

        simpleAttributesWithValConcise: function() {

            var self = this;
            var keepAttributes = _.filter(this.simpleKeys(), function(key) {

                var value = self.attributes[key];
                var type = RealTypeOf(value);
             
                if (key === 'required') {
                    // Only show required if true
                    return value;
                }
                else if (key === 'id') {
                    // Never show
                    return false;
                }
                else if (key === 'dollarschema') {
                    // Never show
                    return false;
                }
                else {
                    return (value != '' && 
                        type != TypeEnum.UNDEFINED &&
                        type != TypeEnum.NULL);
                }
            });

            return _.pick(this.attributes, keepAttributes);
        },

        initialize: function() {

            this.set({
                properties: new SchemaPairList()
            });
            this.set({
                items: new SchemaPairList()
            });
            this.set({
                type: new TypeList()
            });
            this.set({
                extensions: new SchemaPairList()
            });
        },

        clearItems: function() {
            this.set({
                'items': new SchemaPairList()
            });
        },

        addNewProperty: function(aKey) {

            var schemaPair = new SchemaPair({
                key: aKey,
                schema: new Schema()
            });
            this.addProperty(schemaPair);
        },

        addNewItem: function() {
            var schemaPair = new SchemaPair({
                schema: new Schema()
            });
            this.get('items').add(schemaPair);

            if (!this.get('type').isArray()) {
                /* Automatically make this schema an array 
          since we're adding an item. */
                var t = new Type({
                    t: TypeEnum.ARRAY
                });
                this.get('type').add(t);
            }
        },

        addItemCount: function(aItemCount) {

            if ('' == aItemCount) {
                // User didn't request any Items.
                return true;
            }

            if (isNaN(aItemCount)) {
                // User provided bad value.
                return false;
            }

            for (var i = 0; i < aItemCount; i++) {
                var schemaPair = new SchemaPair({
                    schema: new Schema()
                });
                this.get('items').add(schemaPair);
            }

            return (aItemCount == i);
        },

        validate: function(attrs) {

            // Number
            if (attrs.minimum) {
                if (isNaN(attrs.minimum)) {

                    return -1;
                }
            }
            if (attrs.maximum) {
                if (isNaN(attrs.maximum)) {
                    return -1;
                }
            }
            if (attrs.minimum && attrs.maximum) {
                if (attrs.maximum < attrs.minimum) {
                    return -1;
                }
            }
            // Array
            if (attrs.minitems) {
                if (isNaN(attrs.minitems)) {
                    return -1;
                }
            }
            if (attrs.maxitems) {
                if (isNaN(attrs.maxitems)) {
                    return -1;
                }
            }
            if (attrs.minitems && attrs.maxitems) {
                if (attrs.maxitems < attrs.minitems) {
                    return -1;
                }
            }
        },

        addOrReplaceProperty: function(aSchemaPair) {
            if (!aSchemaPair) {
                return;
            }

            var existing = this.get('properties').filterByName(aSchemaPair.get('key'));

            if (existing && existing.length > 0) {
                this.get('properties').remove(existing);
                this.get('properties').add(aSchemaPair);
            } else {
                this.get('properties').add(aSchemaPair);
            }
        },

        addOrReplaceProperties: function(aSchemaPairs) {

            if (!aSchemaPairs) {
                return;
            }
            for (var i = 0; i < aSchemaPairs.length; i++) {
                this.addOrReplaceProperty(aSchemaPairs[i]);
            }
        },

        addProperty: function(aSchemaPair) {
            if (!aSchemaPair) {
                return;
            }
            this.get('properties').add(aSchemaPair);
        },

        addItem: function(aSchemaPair) {
            if (!aSchemaPair) {
                return;
            }
            /* Don't check for duplicates because of Tuple-Typing.
            Just append the item to the collection. */
            this.get('items').add(aSchemaPair);
        },

        addItems: function(aSchemaPairs) {
            if (!aSchemaPairs) {
                return;
            }

            for (var i = 0; i < aSchemaPairs.length; i++) {
                this.addItem(aSchemaPairs[i]);
            }
        },

        addExtension: function(aSchemaPair) {
            if (!aSchemaPair) {
                return;
            }
            this.get('extensions').add(aSchemaPair);
        },

        addType: function(aType) {
            if (!aType) {
                return;
            }
            var existing = this.get('type').filterByType(aType.get('t'));

            if (!existing || existing.length <= 0) {
                this.get('type').add(aType);
            }
        },

        addTypes: function(aTypes) {
            if (!aTypes) {
                return;
            }
            for (var i = 0; i < aTypes.length; i++) {
                this.addType(aTypes[i]);
            }
        },

        addExtension: function(aSchemaPair) {
            if (!aSchemaPair) {
                return;
            }
            this.get('extensions').add(aSchemaPair);
        },
    });

});
