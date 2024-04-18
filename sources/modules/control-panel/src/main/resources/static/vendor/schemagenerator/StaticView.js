$(document).ready(function() {


    SchemaStaticV = Backbone.View.extend({
        sb: [],
        l: 0,
        level: 0,

        setLevel: function(l) {
            this.level = l;
        },

        resetBuffer: function() {
            this.sb = [];
            this.l = 0;
        },

        getIndent: function(l) {
            var s = '';
            l = (l == undefined ? this.level : l);
            for (var i = 0; i < l; i++) {
                s += '\t';
            }
            return s;
        },

        render: function() {
            this.resetBuffer();
            var indent = this.getIndent();

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
                this.sb[this.l++] = indent + this.makeAttribute('type', tlv.render(), false);
            }

            var obj = _.pick(SchemaAttributes, this.model.simpleKeysWithVal());

            for (var key in obj) {
                var attrObj = obj[key];
                $(attrObj['ref'], this.el).show();
                last = attrObj['ref'];

                var type = RealTypeOf(this.model.get(key));
                var hasQuotes = (type == TypeEnum.STRING);
                this.sb[this.l++] = indent + this.makeAttribute(attrObj['name'], this.model.get(key), hasQuotes);
            }

            var unattachedE = undefined;
            if (v.Extensions.length > 0) {
                var eSPLView = new SchemaPairLStaticV({
                    collection: v.Extensions,
                    className: 'Extensions'
                });
                eSPLView.setLevel(this.level);
                this.sb[this.l++] = indent + this.makeAttribute('extends', eSPLView.render(), false);
            }

            if (v.Properties.length > 0) {
                var pSPLView = new SchemaPairLStaticV({
                    collection: v.Properties,
                    className: 'Properties'
                });
                pSPLView.setLevel(this.level);
                this.sb[this.l++] = indent + this.makeAttribute('properties', pSPLView.render(), false);
            }

            if (v.Items.length > 0) {
                var iSPLView = new SchemaPairLStaticV({
                    collection: v.Items,
                    className: 'Items'
                });
                iSPLView.setLevel(this.level);
                this.sb[this.l++] = indent + this.makeAttribute('items', iSPLView.render(), false);
            }

            return this.sb.join(',\n');
        },

        makeAttribute: function(attribute, value, hasQuotes) {
            if (hasQuotes) {
                return ('"' + attribute + '": "' + value + '"');
            }
            return ('"' + attribute + '":' + value);

        }
    });

    SchemaPairStaticV = Backbone.View.extend({
        sb: [],
        l: 0,
        level: 0,
        last: false,

        setLevel: function(l) {
            this.level = l;
        },

        resetBuffer: function() {
            this.sb = [];
            this.l = 0;
        },

        getIndent: function(l) {
            var s = '';
            l = (l == undefined ? this.level : l);
            for (var i = 0; i < l; i++) {
                s += '\t';
            }
            return s;
        },

        render: function() {
            var indent = this.getIndent();
            this.resetBuffer();

            var root = this.model.get('root');

            var v = {
                Key: this.model.get('key'),
                DataLevel: this.level,
            };

            if (root) {
                this.last = true;
            }

            if (v.Key) {
                this.sb[this.l++] = indent + '"' + v.Key + '": {';
            } else {
                this.sb[this.l++] = indent + '{';
            }

            var sv = new SchemaStaticV({
                model: this.model.get('schema')
            });
            sv.setLevel(this.level + 1);

            var eol = ''
            if (this.last) {
                eol += '\n';
            } else {
                eol += ',\n';
            }

            this.sb[this.l++] = sv.render();
            this.sb[this.l++] = (indent + '}' + eol);

            return this.sb.join('\n');
        }
    });


    SchemaPairLStaticV = Backbone.View.extend({
        sb: [],
        l: 0,
        level: 0,
        className: '',

        setLevel: function(l) {
            this.level = l;
        },

        resetBuffer: function() {
            this.sb = [];
            this.l = 0;
        },

        getIndent: function(l) {
            var s = '';
            l = (l == undefined ? this.level : l);
            for (var i = 0; i < l; i++) {
                s += '\t';
            }
            return s;
        },

        render: function() {
            var self = this;
            var pSchemas = (this.className == 'Properties');
            var iSchemas = (this.className == 'Items');
            var eSchemas = (this.className == 'Extensions');
            var tupleTyping = ((iSchemas || eSchemas) && (this.collection.length > 1));
            var indent = this.getIndent();

            this.resetBuffer();

            if (pSchemas) {
                this.sb[this.l++] = '{\n';
            } else {

                if (tupleTyping) {
                    this.sb[this.l++] = '[\n';
                } else {
                    this.sb[this.l++] = '\n';
                }
            }

            var nestedLevel = (this.level + 1);

            _(this.collection.models).each(function(sp) {
                var index = this.collection.indexOf(sp);
                var isLast = (index == (this.collection.length - 1));

                var spv = new SchemaPairStaticV({
                    model: sp
                });
                spv.setLevel(nestedLevel);
                if (isLast) {
                    spv.last = true;
                }
                this.sb[this.l++] = spv.render();

            }, this);

            if (pSchemas) {
                this.sb[this.l++] = indent + '}';
            } else {

                if (tupleTyping) {
                    this.sb[this.l++] = indent + ']';
                } else {
                    this.sb[this.l++] = indent + '\n';
                }
            }

            return this.sb.join('');
        }
    });


    TypeLStaticV = Backbone.View.extend({
        sb: [],
        l: 0,
        level: 0,

        setLevel: function(l) {
            this.level = l;
        },

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
