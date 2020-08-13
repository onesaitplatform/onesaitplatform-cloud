var svgEditorLang_ga = (function () {
  'use strict';

  var lang_ga = {
    lang: 'ga',
    dir: 'ltr',
    common: {
      ok: 'Sábháil',
      cancel: 'Cealaigh',
      key_backspace: 'backspace',
      key_del: 'delete',
      key_down: 'down',
      key_up: 'up',
      more_opts: 'More Options',
      url: 'URL',
      width: 'Width',
      height: 'Height'
    },
    misc: {
      powered_by: 'Powered by'
    },
    ui: {
      toggle_stroke_tools: 'Show/hide more stroke tools',
      palette_info: 'Cliceáil chun athrú a líonadh dath, aistriú-cliceáil chun dath a athrú stróc',
      zoom_level: 'Athraigh súmáil leibhéal',
      panel_drag: 'Click to show hide',
      quality: 'Quality:',
      pathNodeTooltip: 'Drag node to move it. Double-click node to change segment type',
      pathCtrlPtTooltip: 'Drag control point to adjust curve properties',
      pick_stroke_paint_opacity: 'Pick a Stroke Paint and Opacity',
      pick_fill_paint_opacity: 'Pick a Fill Paint and Opacity'
    },
    properties: {
      id: 'Identify the element',
      fill_color: 'Athraigh an dath a líonadh',
      stroke_color: 'Dath stróc Athrú',
      stroke_style: 'Athraigh an stíl Fleasc stróc',
      stroke_width: 'Leithead stróc Athrú',
      pos_x: 'Change X coordinate',
      pos_y: 'Change Y coordinate',
      linecap_butt: 'Linecap: Butt',
      linecap_round: 'Linecap: Round',
      linecap_square: 'Linecap: Square',
      linejoin_bevel: 'Linejoin: Bevel',
      linejoin_miter: 'Linejoin: Miter',
      linejoin_round: 'Linejoin: Round',
      angle: 'Uillinn rothlaithe Athrú',
      blur: 'Change gaussian blur value',
      opacity: 'Athraigh roghnaithe teimhneacht mír',
      circle_cx: 'Athraigh an ciorcal a chomhordú CX',
      circle_cy: 'Athraigh an ciorcal a chomhordú ga',
      circle_r: 'Athraigh an ciorcal&#39;s ga',
      ellipse_cx: 'Athraigh Éilips&#39;s CX a chomhordú',
      ellipse_cy: 'Athraigh an Éilips a chomhordú ga',
      ellipse_rx: 'Éilips Athraigh an gha x',
      ellipse_ry: 'Éilips Athraigh an gha y',
      line_x1: 'Athraigh an líne tosaigh a chomhordú x',
      line_x2: 'Athraigh an líne deireadh x chomhordú',
      line_y1: 'Athraigh an líne tosaigh a chomhordú y',
      line_y2: 'Athrú ar líne deireadh y chomhordú',
      rect_height: 'Airde dronuilleog Athrú',
      rect_width: 'Leithead dronuilleog Athrú',
      corner_radius: 'Athraigh Dronuilleog Cúinne na Ga',
      image_width: 'Leithead íomhá Athrú',
      image_height: 'Airde íomhá Athrú',
      image_url: 'Athraigh an URL',
      node_x: "Change node's x coordinate",
      node_y: "Change node's y coordinate",
      seg_type: 'Change Segment type',
      straight_segments: 'Straight',
      curve_segments: 'Curve',
      text_contents: 'Inneachar Athraigh téacs',
      font_family: 'Athraigh an Cló Teaghlaigh',
      font_size: 'Athraigh Clómhéid',
      bold: 'Trom Téacs',
      italic: 'Iodálach Téacs'
    },
    tools: {
      main_menu: 'Main Menu',
      bkgnd_color_opac: 'Dath cúlra Athraigh / teimhneacht',
      connector_no_arrow: 'No arrow',
      fitToContent: 'Fit to Content',
      fit_to_all: 'Laghdaigh do gach ábhar',
      fit_to_canvas: 'Laghdaigh ar chanbhás',
      fit_to_layer_content: 'Laghdaigh shraith ábhar a',
      fit_to_sel: 'Laghdaigh a roghnú',
      align_relative_to: 'Ailínigh i gcomparáid leis ...',
      relativeTo: 'i gcomparáid leis:',
      page: 'leathanach',
      largest_object: 'réad is mó',
      selected_objects: 'réada tofa',
      smallest_object: 'lú réad',
      new_doc: 'Íomhá Nua',
      open_doc: 'Íomhá Oscailte',
      export_img: 'Export',
      save_doc: 'Sábháil Íomhá',
      import_doc: 'Import Image',
      align_to_page: 'Align Element to Page',
      align_bottom: 'Cineál Bun',
      align_center: 'Ailínigh sa Lár',
      align_left: 'Ailínigh ar Chlé',
      align_middle: 'Cineál Middle',
      align_right: 'Ailínigh ar Dheis',
      align_top: 'Cineál Barr',
      mode_select: 'Roghnaigh Uirlis',
      mode_fhpath: 'Phionsail Uirlis',
      mode_line: 'Uirlis Líne',
      mode_rect: 'Rectangle Tool',
      mode_square: 'Square Tool',
      mode_fhrect: 'Saor Hand Dronuilleog',
      mode_ellipse: 'Éilips',
      mode_circle: 'Ciorcal',
      mode_fhellipse: 'Free-Hand Ellipse',
      mode_path: 'Path Tool',
      mode_text: 'Téacs Uirlis',
      mode_image: 'Íomhá Uirlis',
      mode_zoom: 'Zúmáil Uirlis',
      no_embed: 'NOTE: This image cannot be embedded. It will depend on this path to be displayed',
      undo: 'Cealaigh',
      redo: 'Athdhéan',
      tool_source: 'Cuir Foinse',
      wireframe_mode: 'Wireframe Mode',
      clone: 'Clone Element(s)',
      del: 'Delete Element(s)',
      group_elements: 'Eilimintí Grúpa',
      make_link: 'Make (hyper)link',
      set_link_url: 'Set link URL (leave empty to remove)',
      to_path: 'Convert to Path',
      reorient_path: 'Reorient path',
      ungroup: 'Eilimintí Díghrúpáil',
      docprops: 'Doiciméad Airíonna',
      move_bottom: 'Téigh go Bun',
      move_top: 'Téigh go Barr',
      node_clone: 'Clone Node',
      node_delete: 'Delete Node',
      node_link: 'Link Control Points',
      add_subpath: 'Add sub-path',
      openclose_path: 'Open/close sub-path',
      source_save: 'Sábháil',
      cut: 'Cut',
      copy: 'Copy',
      paste: 'Paste',
      paste_in_place: 'Paste in Place',
      "delete": 'Delete',
      group: 'Group',
      move_front: 'Bring to Front',
      move_up: 'Bring Forward',
      move_down: 'Send Backward',
      move_back: 'Send to Back'
    },
    layers: {
      layer: 'Layer',
      layers: 'Layers',
      del: 'Scrios Sraith',
      move_down: 'Bog Sraith Síos',
      "new": 'Sraith Nua',
      rename: 'Athainmnigh Sraith',
      move_up: 'Bog Sraith Suas',
      dupe: 'Duplicate Layer',
      merge_down: 'Merge Down',
      merge_all: 'Merge All',
      move_elems_to: 'Move elements to:',
      move_selected: 'Move selected elements to a different layer'
    },
    config: {
      image_props: 'Image Properties',
      doc_title: 'Title',
      doc_dims: 'Canvas Dimensions',
      included_images: 'Included Images',
      image_opt_embed: 'Embed data (local files)',
      image_opt_ref: 'Use file reference',
      editor_prefs: 'Editor Preferences',
      icon_size: 'Icon size',
      language: 'Language',
      background: 'Editor Background',
      editor_img_url: 'Image URL',
      editor_bg_note: 'Note: Background will not be saved with image.',
      icon_large: 'Large',
      icon_medium: 'Medium',
      icon_small: 'Small',
      icon_xlarge: 'Extra Large',
      select_predefined: 'Roghnaigh réamhshainithe:',
      units_and_rulers: 'Units & Rulers',
      show_rulers: 'Show rulers',
      base_unit: 'Base Unit:',
      grid: 'Grid',
      snapping_onoff: 'Snapping on/off',
      snapping_stepsize: 'Snapping Step-Size:',
      grid_color: 'Grid color'
    },
    notification: {
      invalidAttrValGiven: 'Invalid value given',
      noContentToFitTo: 'No content to fit to',
      dupeLayerName: 'There is already a layer named that!',
      enterUniqueLayerName: 'Please enter a unique layer name',
      enterNewLayerName: 'Please enter the new layer name',
      layerHasThatName: 'Layer already has that name',
      QmoveElemsToLayer: "Move selected elements to layer '%s'?",
      QwantToClear: 'Do you want to clear the drawing?\nThis will also erase your undo history!',
      QwantToOpen: 'Do you want to open a new file?\nThis will also erase your undo history!',
      QerrorsRevertToSource: 'There were parsing errors in your SVG source.\nRevert back to original SVG source?',
      QignoreSourceChanges: 'Ignore changes made to SVG source?',
      featNotSupported: 'Feature not supported',
      enterNewImgURL: 'Enter the new image URL',
      defsFailOnSave: 'NOTE: Due to a bug in your browser, this image may appear wrong (missing gradients or elements). It will however appear correct once actually saved.',
      loadingImage: 'Loading image, please wait...',
      saveFromBrowser: "Select 'Save As...' in your browser (possibly via file menu or right-click context-menu) to save this image as a %s file.",
      noteTheseIssues: 'Also note the following issues: ',
      unsavedChanges: 'There are unsaved changes.',
      enterNewLinkURL: 'Enter the new hyperlink URL',
      errorLoadingSVG: 'Error: Unable to load SVG data',
      URLLoadFail: 'Unable to load from URL',
      retrieving: 'Retrieving \'%s\' ...',
      popupWindowBlocked: 'Popup window may be blocked by browser',
      exportNoBlur: 'Blurred elements will appear as un-blurred',
      exportNoforeignObject: 'foreignObject elements will not appear',
      exportNoDashArray: 'Strokes will appear filled',
      exportNoText: 'Text may not appear as expected'
    }
  };

  return lang_ga;

}());
