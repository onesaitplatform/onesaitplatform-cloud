
var Languages = function() {

	const languajes = [
		{
			"code": "aa",
			"description": "Afar"
		},
		{
			"code": "ab",
			"description": "Abkhazian"
		},
		{
			"code": "af",
			"description": "Afrikaans"
		},
		{
			"code": "ak",
			"description": "Akan"
		},
		{
			"code": "sq",
			"description": "Albanian"
		},
		{
			"code": "am",
			"description": "Amharic"
		},
		{
			"code": "ar",
			"description": "Arabic"
		},
		{
			"code": "an",
			"description": "Aragonese"
		},
		{
			"code": "hy",
			"description": "Armenian"
		},
		{
			"code": "as",
			"description": "Assamese"
		},
		{
			"code": "av",
			"description": "Avaric"
		},
		{
			"code": "ae",
			"description": "Avestan"
		},
		{
			"code": "ay",
			"description": "Aymara"
		},
		{
			"code": "az",
			"description": "Azerbaijani"
		},
		{
			"code": "ba",
			"description": "Bashkir"
		},
		{
			"code": "bm",
			"description": "Bambara"
		},
		{
			"code": "eu",
			"description": "Basque"
		},
		{
			"code": "be",
			"description": "Belarusian"
		},
		{
			"code": "bn",
			"description": "Bengali"
		},
		{
			"code": "bh",
			"description": "Bihari languages"
		},
		{
			"code": "bi",
			"description": "Bislama"
		},
		{
			"code": "bo",
			"description": "Tibetan"
		},
		{
			"code": "bs",
			"description": "Bosnian"
		},
		{
			"code": "br",
			"description": "Breton"
		},
		{
			"code": "bg",
			"description": "Bulgarian"
		},
		{
			"code": "my",
			"description": "Burmese"
		},
		{
			"code": "ca",
			"description": "Catalan; Valencian"
		},
		{
			"code": "cs",
			"description": "Czech"
		},
		{
			"code": "ch",
			"description": "Chamorro"
		},
		{
			"code": "ce",
			"description": "Chechen"
		},
		{
			"code": "zh",
			"description": "Chinese"
		},
		{
			"code": "cu",
			"description": "Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic"
		},
		{
			"code": "cv",
			"description": "Chuvash"
		},
		{
			"code": "kw",
			"description": "Cornish"
		},
		{
			"code": "co",
			"description": "Corsican"
		},
		{
			"code": "cr",
			"description": "Cree"
		},
		{
			"code": "cy",
			"description": "Welsh"
		},
		{
			"code": "cs",
			"description": "Czech"
		},
		{
			"code": "da",
			"description": "Danish"
		},
		{
			"code": "de",
			"description": "German"
		},
		{
			"code": "dv",
			"description": "Divehi; Dhivehi; Maldivian"
		},
		{
			"code": "nl",
			"description": "Dutch; Flemish"
		},
		{
			"code": "dz",
			"description": "Dzongkha"
		},
		{
			"code": "el",
			"description": "Greek, Modern (1453-)"
		},
		{
			"code": "en",
			"description": "English"
		},
		{
			"code": "eo",
			"description": "Esperanto"
		},
		{
			"code": "et",
			"description": "Estonian"
		},
		{
			"code": "eu",
			"description": "Basque"
		},
		{
			"code": "ee",
			"description": "Ewe"
		},
		{
			"code": "fo",
			"description": "Faroese"
		},
		{
			"code": "fa",
			"description": "Persian"
		},
		{
			"code": "fj",
			"description": "Fijian"
		},
		{
			"code": "fi",
			"description": "Finnish"
		},		
		{
			"code": "fr",
			"description": "French"
		},
		{
			"code": "fy",
			"description": "Western Frisian"
		},
		{
			"code": "ff",
			"description": "Fulah"
		},
		{
			"code": "ka",
			"description": "Georgian"
		},
		{
			"code": "de",
			"description": "German"
		},
		{
			"code": "gd",
			"description": "Gaelic; Scottish Gaelic"
		},
		{
			"code": "ga",
			"description": "Irish"
		},
		{
			"code": "gl",
			"description": "Galician"
		},
		{
			"code": "gv",
			"description": "Manx"
		},
		{
			"code": "el",
			"description": "Greek, Modern (1453-)"
		},
		{
			"code": "gn",
			"description": "Guarani"
		},
		{
			"code": "gu",
			"description": "Gujarati"
		},
		{
			"code": "ht",
			"description": "Haitian; Haitian Creole"
		},
		{
			"code": "ha",
			"description": "Hausa"
		},
		{
			"code": "he",
			"description": "Hebrew"
		},
		{
			"code": "hz",
			"description": "Herero"
		},
		{
			"code": "hi",
			"description": "Hindi"
		},
		{
			"code": "ho",
			"description": "Hiri Motu"
		},
		{
			"code": "hr",
			"description": "Croatian"
		},
		{
			"code": "hu",
			"description": "Hungarian"
		},
		{
			"code": "hy",
			"description": "Armenian"
		},
		{
			"code": "ig",
			"description": "Igbo"
		},
		{
			"code": "is",
			"description": "Icelandic"
		},
		{
			"code": "io",
			"description": "Ido"
		},
		{
			"code": "ii",
			"description": "Sichuan Yi; Nuosu"
		},
		{
			"code": "iu",
			"description": "Inuktitut"
		},
		{
			"code": "ie",
			"description": "Interlingue; Occidental"
		},
		{
			"code": "ia",
			"description": "Interlingua (International Auxiliary Language Association)"
		},
		{
			"code": "id",
			"description": "Indonesian"
		},
		{
			"code": "ik",
			"description": "Inupiaq"
		},
		{
			"code": "is",
			"description": "Icelandic"
		},
		{
			"code": "it",
			"description": "Italian"
		},
		{
			"code": "jv",
			"description": "Javanese"
		},
		{
			"code": "ja",
			"description": "Japanese"
		},
		{
			"code": "kl",
			"description": "Kalaallisut; Greenlandic"
		},
		{
			"code": "kn",
			"description": "Kannada"
		},
		{
			"code": "ks",
			"description": "Kashmiri"
		},
		{
			"code": "ka",
			"description": "Georgian"
		},
		{
			"code": "kr",
			"description": "Kanuri"
		},
		{
			"code": "kk",
			"description": "Kazakh"
		},
		{
			"code": "km",
			"description": "Central Khmer"
		},
		{
			"code": "ki",
			"description": "Kikuyu; Gikuyu"
		},
		{
			"code": "rw",
			"description": "Kinyarwanda"
		},
		{
			"code": "ky",
			"description": "Kirghiz; Kyrgyz"
		},
		{
			"code": "kv",
			"description": "Komi"
		},
		{
			"code": "kg",
			"description": "Kongo"
		},
		{
			"code": "ko",
			"description": "Korean"
		},
		{
			"code": "kj",
			"description": "Kuanyama; Kwanyama"
		},
		{
			"code": "ku",
			"description": "Kurdish"
		},
		{
			"code": "lo",
			"description": "Lao"
		},
		{
			"code": "la",
			"description": "Latin"
		},
		{
			"code": "lv",
			"description": "Latvian"
		},
		{
			"code": "li",
			"description": "Limburgan; Limburger; Limburgish"
		},
		{
			"code": "ln",
			"description": "Lingala"
		},
		{
			"code": "lt",
			"description": "Lithuanian"
		},
		{
			"code": "lb",
			"description": "Luxembourgish; Letzeburgesch"
		},
		{
			"code": "lu",
			"description": "Luba-Katanga"
		},
		{
			"code": "lg",
			"description": "Ganda"
		},
		{
			"code": "mk",
			"description": "Macedonian"
		},
		{
			"code": "mh",
			"description": "Marshallese"
		},
		{
			"code": "ml",
			"description": "Malayalam"
		},
		{
			"code": "mi",
			"description": "Maori"
		},
		{
			"code": "mr",
			"description": "Marathi"
		},
		{
			"code": "ms",
			"description": "Malay"
		},
		{
			"code": "mk",
			"description": "Macedonian"
		},
		{
			"code": "mg",
			"description": "Malagasy"
		},
		{
			"code": "mt",
			"description": "Maltese"
		},
		{
			"code": "mn",
			"description": "Mongolian"
		},
		{
			"code": "mi",
			"description": "Maori"
		},
		{
			"code": "ms",
			"description": "Malay"
		},
		{
			"code": "my",
			"description": "Burmese"
		},
		{
			"code": "na",
			"description": "Nauru"
		},
		{
			"code": "nv",
			"description": "Navajo; Navaho"
		},
		{
			"code": "nr",
			"description": "Ndebele, South; South Ndebele"
		},
		{
			"code": "nd",
			"description": "Ndebele, North; North Ndebele"
		},
		{
			"code": "ng",
			"description": "Ndonga"
		},
		{
			"code": "ne",
			"description": "Nepali"
		},
		{
			"code": "nl",
			"description": "Dutch; Flemish"
		},
		{
			"code": "nn",
			"description": "Norwegian Nynorsk; Nynorsk, Norwegian"
		},
		{
			"code": "nb",
			"description": "Bokmål, Norwegian; Norwegian Bokmål"
		},
		{
			"code": "no",
			"description": "Norwegian"
		},
		{
			"code": "ny",
			"description": "Chichewa; Chewa; Nyanja"
		},
		{
			"code": "oc",
			"description": "Occitan (post 1500)"
		},
		{
			"code": "oj",
			"description": "Ojibwa"
		},
		{
			"code": "or",
			"description": "Oriya"
		},
		{
			"code": "om",
			"description": "Oromo"
		},
		{
			"code": "os",
			"description": "Ossetian; Ossetic"
		},
		{
			"code": "pa",
			"description": "Panjabi; Punjabi"
		},
		{
			"code": "fa",
			"description": "Persian"
		},
		{
			"code": "pi",
			"description": "Pali"
		},
		{
			"code": "pl",
			"description": "Polish"
		},
		{
			"code": "pt",
			"description": "Portuguese"
		},
		{
			"code": "ps",
			"description": "Pushto; Pashto"
		},
		{
			"code": "qu",
			"description": "Quechua"
		},
		{
			"code": "rm",
			"description": "Romansh"
		},
		{
			"code": "ro",
			"description": "Romanian; Moldavian; Moldovan"
		},
		{
			"code": "ro",
			"description": "Romanian; Moldavian; Moldovan"
		},
		{
			"code": "rn",
			"description": "Rundi"
		},
		{
			"code": "ru",
			"description": "Russian"
		},
		{
			"code": "sg",
			"description": "Sango"
		},
		{
			"code": "sa",
			"description": "Sanskrit"
		},
		{
			"code": "si",
			"description": "Sinhala; Sinhalese"
		},
		{
			"code": "sk",
			"description": "Slovak"
		},
		{
			"code": "sk",
			"description": "Slovak"
		},
		{
			"code": "sl",
			"description": "Slovenian"
		},
		{
			"code": "se",
			"description": "Northern Sami"
		},
		{
			"code": "sm",
			"description": "Samoan"
		},
		{
			"code": "sn",
			"description": "Shona"
		},
		{
			"code": "sd",
			"description": "Sindhi"
		},
		{
			"code": "so",
			"description": "Somali"
		},
		{
			"code": "st",
			"description": "Sotho, Southern"
		},
		{
			"code": "es",
			"description": "Spanish"
		},
		{
			"code": "sq",
			"description": "Albanian"
		},
		{
			"code": "sc",
			"description": "Sardinian"
		},
		{
			"code": "sr",
			"description": "Serbian"
		},
		{
			"code": "ss",
			"description": "Swati"
		},
		{
			"code": "su",
			"description": "Sundanese"
		},
		{
			"code": "sw",
			"description": "Swahili"
		},
		{
			"code": "sv",
			"description": "Swedish"
		},
		{
			"code": "ty",
			"description": "Tahitian"
		},
		{
			"code": "ta",
			"description": "Tamil"
		},
		{
			"code": "tt",
			"description": "Tatar"
		},
		{
			"code": "te",
			"description": "Telugu"
		},
		{
			"code": "tg",
			"description": "Tajik"
		},
		{
			"code": "tl",
			"description": "Tagalog"
		},
		{
			"code": "th",
			"description": "Thai"
		},
		{
			"code": "bo",
			"description": "Tibetan"
		},
		{
			"code": "ti",
			"description": "Tigrinya"
		},
		{
			"code": "to",
			"description": "Tonga (Tonga Islands)"
		},
		{
			"code": "tn",
			"description": "Tswana"
		},
		{
			"code": "ts",
			"description": "Tsonga"
		},
		{
			"code": "tk",
			"description": "Turkmen"
		},
		{
			"code": "tr",
			"description": "Turkish"
		},
		{
			"code": "tw",
			"description": "Twi"
		},
		{
			"code": "ug",
			"description": "Uighur; Uyghur"
		},
		{
			"code": "uk",
			"description": "Ukrainian"
		},
		{
			"code": "ur",
			"description": "Urdu"
		},
		{
			"code": "uz",
			"description": "Uzbek"
		},
		{
			"code": "ve",
			"description": "Venda"
		},
		{
			"code": "vi",
			"description": "Vietnamese"
		},
		{
			"code": "vo",
			"description": "Volapük"
		},
		{
			"code": "cy",
			"description": "Welsh"
		},
		{
			"code": "wa",
			"description": "Walloon"
		},
		{
			"code": "wo",
			"description": "Wolof"
		},
		{
			"code": "xh",
			"description": "Xhosa"
		},
		{
			"code": "yi",
			"description": "Yiddish"
		},
		{
			"code": "yo",
			"description": "Yoruba"
		},
		{
			"code": "za",
			"description": "Zhuang; Chuang"
		},
		{
			"code": "zh",
			"description": "Chinese"
		},
		{
			"code": "zu",
			"description": "Zulu"
		}
	]
	//  PUBLIC FUNCTIONS 
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		getListLanguajes: function() {
			return languajes;
		},
		createOptionsSelectPicker: function(idSelect) {

			let select = $('#' + idSelect);
			if (select) {
				select.empty();
				for (var i = 0; i < languajes.length; i++) {
					select.append('<option  value="' + languajes[i].code + '" id="' + languajes[i].code + '">' + languajes[i].description + ' </option> ');
				}
				select.selectpicker('refresh');
				select.selectpicker('render');
			}
		}
	}

}();
 
