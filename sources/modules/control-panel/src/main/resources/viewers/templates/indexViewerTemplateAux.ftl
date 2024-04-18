<!DOCTYPE html>
<html>
	<head>
		<title>Fullscreen map with Toolbar Onesait Platform</title>
		<meta name="viewport" content="initial-scale=1.0">
		<meta charset="utf-8">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/css/bootstrap.min.css" />
	<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.1/css/all.css" integrity="sha384-gfdkjb5BdAXd+lj+gudLWI+BXq4IuLW5IT+brZEZsLFm++aCMlF1V92rMkPaX4PP" crossorigin="anonymous"/>
	<link rel="stylesheet" type="text/css" href="${widgetcss}">
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.13.1/css/bootstrap-select.css" />
	
	<style>
	.cesium-viewer-bottom {
        display: none !important;
      }
	canvas {
        width: 100%;
        height:100%;
      }
	</style>
	
	</head>
	<body style="overflow:hidden">
	
	<nav class="navbar navbar-default bg-onesait">
	  <div class="container-fluid">
		<!-- Brand and toggle get grouped for better mobile display -->
		<div class="navbar-header">
		  <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
			<span class="sr-only">Toggle navigation</span>
			<span class="icon-bar"></span>
			<span class="icon-bar"></span>
			<span class="icon-bar"></span>
		  </button>
		  <a class="navbar-brand" href="#">Onesait</a>
		</div>
		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
		  <div class="nav navbar-nav navbar-form navbar-left">
		   
		   <div class="btn-toolbar" role="toolbar" aria-label="Toolbar with button groups">
				
				<div class="btn-group" role="group" aria-label="First group">
					<button type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="modal" data-target="#myModal" title="Cesium settings"><i class="fa fa-bars fa-fw"></i></button>
				</div>
			
				<div class="btn-group" role="group" aria-label="Second group">
						<button id="drawPoint" type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="tooltip" data-container="body" data-placement="bottom" title="Draw Point" onclick="drawPointClick()"><i class="fas fa-map-marker-alt"></i></button>
						<button id="drawLine" type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="tooltip" data-container="body" data-placement="bottom" title="Draw Line" onclick="drawLineClick()"><i class="fa fa-grip-lines"></i></button>
						<button id="drawPolygon" type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="tooltip" data-container="body" data-placement="bottom" title="Draw Polygon" onclick="drawPolygonClick()"><i class="fas fa-draw-polygon"></i></button>
						<button id="drawPolygonFree" type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="tooltip" data-container="body" data-placement="bottom" title="Draw Polygon Free Hand" onclick="drawPolygonFreehand()"><i class="fas fa-draw-polygon"></i> <i class="fas fa-pencil-alt"></i></button>
						<button id="rule" type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="tooltip" data-container="body" data-placement="bottom" title="Rule" onclick="measureRuler()"><i class="fa fa-ruler-horizontal"></i></button>
				</div>		
				<div class="btn-group" role="group" aria-label="Second group">
						<button id="less_zoom" type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="tooltip" data-container="body" data-placement="bottom" title="-" onclick="zoomOut()"><i class="fa fa-minus"></i></button>
						<button id="more_zoom" type="button" class="btn btn-sm btn-circle btn-outline btn-primary" data-toggle="tooltip" data-container="body" data-placement="bottom" title="+" onclick="zoomIn()"><i class="fa fa-plus"></i></button>
						
				</div>		
			</div>
				
		  </div>
		  <form class="navbar-form navbar-left">
			<div class="form-group">
				<div class="input-group">
					<input type="text" class="form-control input-sm" placeholder="Search for..."/>
					<span class="input-group-btn input-group-sm ">
						<button type="submit" class="btn btn-sm btn-default"><i class="fa fa-search"></i></button>
					</span>
				</div>
			</div>      
		  </form>
		  <div class="nav navbar-nav navbar-right">       
			<div class="input-group input-group-sm">
				<select id="layers" class="selectpicker input-sm" multiple="multiple" data-live-search="true">
											
				</select>
			</div>				
		  </div>
		</div><!-- /.navbar-collapse -->
	  </div><!-- /.container-fluid -->
	</nav>
	
	
	<div id="myModal" class="modal left fade" tabindex="-1" role="dialog" aria-labelledby="myModal">
	  <div class="modal-dialog" role="document">
		<div class="modal-content">
		  <div class="modal-header">
			<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
			<h4 class="modal-title">Cesium Map Configuration</h4>
		  </div>
		  <div class="modal-body">
			<p>Contenidos...&hellip;</p>
		  </div>
		  <div class="modal-footer">
			<button type="button" class="btn btn-default" data-dismiss="modal">Cerrar</button>
			<button type="button" class="btn btn-primary">Guardar Cambios</button>
		  </div>
		</div><!-- /.modal-content -->
	  </div><!-- /.modal-dialog -->
	</div><!-- /.modal -->
  	
    
	
	<div id="cesiumContainer"></div>
	<div id="statusbar"></div>
	
	<!-- scripts -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.0/js/tether.min.js" integrity="sha384-DztdAPBWPRXSA/3eYEEUWrWCy7G5KFbe8fFjk5JAIxUYHKkDx6Qin1DkWx51bBrb" crossorigin="anonymous" defer></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>
	<script src="${cesiumPath}"></script>	
	<script src="${heatmap}"></script>	
	<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.13.1/js/bootstrap-select.min.js"></script>
	
	</body>
</html>
