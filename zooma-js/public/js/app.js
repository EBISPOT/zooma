(function($){
	"use strict";

	$(document).ready(function(){
		var $search   = $('#zooma-separated-search');
		var $annotate = $('#zooma-separated-suggestions');
		var zooma_url = "http://snarf.ebi.ac.uk:8480/spot/zooma/";


		$search.zooma('autocomplete', {
			'zooma_url' : zooma_url
		}); 
		$annotate.zooma('annotate', {
			'zooma_url' : zooma_url,
			'annotation_source': $search
		});	
	});
})(jQuery);