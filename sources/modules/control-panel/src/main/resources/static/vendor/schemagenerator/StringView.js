$(document).ready(function() {


    SchemaStrV = Backbone.View.extend({

        sb: [],
        l: 0,

        resetBuffer: function() {
            this.sb = [];
            this.l = 0;
        },

        render: function() {
            this.resetBuffer();

            var v = {
                DollarSchema: this.model.get('dollarschema'),
                DollarRef: this.model.get('dollarref'),
                Types: this.model.get('type'),
                Title: this.model.get('title'),
                Name: this.model.get('name'),
                Required: this.model.get('required'),
                Description: this.model.get('description'),
                Properties: this.model.get('properties'),
                Extensions: this.model.get('extensions'),
                Items: this.model.get('items'),
                SchemaId: this.model.get('schemaid'),
                Minimum: this.model.get('minimum'),
                Maximum: this.model.get('maximum'),
                MinItems: this.model.get('minitems'),
                MaxItems: this.model.get('maxitems'),
                DataLevel: this.level,
            };

            if (v.Types.length > 0) {
                var tlv = new TypeLStaticV({
                    collection: v.Types
                });
                tlv.setLevel(this.level);
                this.sb[this.l++] = this.makeAttribute('type', tlv.render(), false);
            }

            var obj = _.pick(SchemaAttributes, this.model.simpleKeysWithVal());

            for (var key in obj) {
                var attrObj = obj[key];
                $(attrObj['ref'], this.el).show();
                last = attrObj['ref'];

                var type = RealTypeOf(this.model.get(key));
                var hasQuotes = (type == TypeEnum.STRING);
                this.sb[this.l++] = this.makeAttribute(attrObj['name'], this.model.get(key), hasQuotes);
            }

            var unattachedE = undefined;
            if (v.Extensions.length > 0) {
                var eSPLView = new SchemaPairLStaticV({
                    collection: v.Extensions,
                    className: 'Extensions'
                });
                eSPLView.setLevel(this.datalevel);
                this.sb[this.l++] = this.makeAttribute('extends', eSPLView.render(), false);
            }

            if (v.Properties.length > 0) {
                var pSPLView = new SchemaPairLStaticV({
                    collection: v.Properties,
                    className: 'Properties'
                });
                pSPLView.setLevel(this.level);
                this.sb[this.l++] = this.makeAttribute('properties', pSPLView.render(), false);
            }

            if (v.Items.length > 0) {
                var iSPLView = new SchemaPairLStaticV({
                    collection: v.Items,
                    className: 'Items'
                });
                iSPLView.setLevel(this.level);
                this.sb[this.l++] = this.makeAttribute('items', iSPLView.render(), false);
            }

            return this.sb.join(',');
        },

        makeAttribute: function(attribute, value, hasQuotes) {
            if (hasQuotes) {
                return ('"' + attribute + '": "' + value + '"');
            }
            return ('"' + attribute + '":' + value);

        }

    });

    SchemaPairStrV = Backbone.View.extend({
        last: false,
        sb: [],
        l: 0,

        resetBuffer: function() {
            this.sb = [];
            this.l = 0;
        },

        render: function() {
            this.resetBuffer();

            var root = this.model.get('root');

            var v = {
                Key: this.model.get('key'),
            };

            if (root) {
                this.last = true;
            }

            if (v.Key) {
                this.sb[this.l++] = '"' + v.Key + '": {';
            } else {
                this.sb[this.l++] = '{';
            }

            var sv = new SchemaStrV({
                model: this.model.get('schema')
            });

            var eol = ''
            if (!this.last) {
                eol += ',';
            }

            this.sb[this.l++] = sv.render();
            this.sb[this.l++] = '}' + eol;

            return this.sb.join('');
        }
    });


    SchemaPairLStrV = Backbone.View.extend({
        className: '',
        sb: [],
        l: 0,

        resetBuffer: function() {
            this.sb = [];
            this.l = 0;
        },

        render: function() {
            var self = this;
            var pSchemas = (this.className == 'Properties');
            var iSchemas = (this.className == 'Items');
            var eSchemas = (this.className == 'Extensions');
            var tupleTyping = ((iSchemas||eSchemas) && (this.collection.length > 1));

            this.resetBuffer();

            if (pSchemas) {
                this.sb[this.l++] = '{';
            } else {

                if (tupleTyping) {
                    this.sb[this.l++] = '[';
                }
            }


            _(this.collection.models).each(function(sp) {
                var index = this.collection.indexOf(sp);
                var isLast = (index == (this.collection.length - 1));

                var spv = new SchemaPairStrV({
                    model: sp
                });

                if (isLast) {
                    spv.last = true;
                }
                this.sb[this.l++] = spv.render();

            }, this);

            if (pSchemas) {
                this.sb[this.l++] = '}';
            } else {

                if (tupleTyping) {
                    this.sb[this.l++] = ']';
                }
            }

            return this.sb.join('');
        }
    });


    TypeLStrV = Backbone.View.extend({
        sb: [],
        l: 0,

        resetBuffer: function() {
            this.sb = [];
            this.l = 0;
        },

        render: function() {
            var self = this;
            var unionType = (this.collection.length > 1);

            this.resetBuffer();

            if (unionType) {
                this.sb[this.l] = '[';
            } else {
                this.sb[this.l] = '';
            }

            _(this.collection.models).each(function(type) {
                var index = this.collection.indexOf(type);
                var isLast = (index == (this.collection.length - 1));

                this.sb[this.l] += ('"' + type.get('t') + '"');
                if (!isLast) {
                    this.sb[this.l] += ',';
                }
            }, this);

            if (unionType) {
                this.sb[this.l] += ']';
            }
            return this.sb[this.l];
        }
    });

});
