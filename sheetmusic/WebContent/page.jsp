<!DOCTYPE html>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" errorPage="error.jsp"%>
<%@ taglib prefix="sheetmusic" uri="WEB-INF/tags.tld"%>
<%@page import="com.jeltechnologies.screenmusic.tags.BaseTag"%>
<%@page import="com.jeltechnologies.utils.StringUtils"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>
<html>
<head>
	<jsp:include page="head.jsp"></jsp:include>
	<link rel="stylesheet" type="text/css" href="files/swiper-11.0.4/swiper-bundle.min.css" />
	<script src="files/swiper-11.0.4/swiper-bundle.min.js"></script>
	<script src="app/page.js"></script>

<style>
	.slidecontainer {
  width: 100%; /* Width of the outside container */
}

/* The slider itself */
.slider {
  -webkit-appearance: none;  /* Override default CSS styles */
  appearance: none;
  width: 100%; /* Full-width */
  height: 25px; /* Specified height */
  background: #d3d3d3; /* Grey background */
  outline: none; /* Remove outline */
  opacity: 0.7; /* Set transparency (for mouse-over effects on hover) */
  -webkit-transition: .2s; /* 0.2 seconds transition on hover */
  transition: opacity .2s;
}

</style>
</head>

<body>
	<div id="swiper-left-top-menu"></div>

	<div id="page-title-container"></div>

	<div id="swiper-right-top-menu">
		<button title="Open Book" onclick="openBookClicked();">
			<icons:icon name="book" cssClass="button-icon" />
		</button>

		<button title="Download these pages" onclick="downloadClicked('sheets');">
			<icons:icon name="download" cssClass="button-icon" />
		</button>

		<button title="Listen to these pages" onclick="downloadClicked('musicxml');">
			<icons:icon name="earbuds" cssClass="button-icon" />
		</button>

		<button title="Remove as favorite" id="favorites-yes" onclick="favoritesPageClicked('yes');">
			<icons:icon name="star-fill" cssClass="button-icon" />
		</button>
		
		<button title="Add as favorite" id="favorites-no" onclick="favoritesPageClicked('no');">
			<icons:icon name="star" cssClass="button-icon" />
		</button>

		<button title="Show full screen" onclick="openFullscreen();">
			<icons:icon name="fullscreen" cssClass="button-icon" />
		</button>

		<button title="Close" onclick="handleCloseAction();">
			<icons:icon name="x" cssClass="button-icon" />
		</button>
	</div>

	<!-- Slider main container -->
	<div class="swiper" id="swiper">
		<!-- Additional required wrapper -->
		<div id="swipper-wrapper" class="swiper-wrapper"></div>
	</div>

	<div class="modal" id="favoriteModal" style="display: none;">
		<h3>
			<span id="favoriteTitle">Favorite</span>
		</h3>
		<table class="modal-table">
			<tr>
				<td width="25%">Name</td>
				<td><input type="text" id="favoriteFormUserLabel" size="40" /> <span class='typcn typcn-times clickButton' onclick="handleFavoriteFormUserLabelClear();"></span></td>
			</tr>
			<tr>
				<td>Book</td>
				<td><span id="favoriteFormFileName"></span></td>
			</tr>
			<tr>
				<td>Page</td>
				<td><span id="favoriteFormStartPage"></span></td>
			</tr>
		</table>
		<div class="favoritemodalbuttons">
			<button type="button" id="favoritesModalOK" onclick="favoritesModalOk();">Ok</button>
			<button type="button" id="favoritesModalDelete" onclick="favoritesModalDelete();">Delete</button>
			<button type="button" onclick="favoritesModalCancel();">Cancel</button>
		</div>
	</div>

	<div class="modal" id="downloadModal" style="display: none;">
		<h3>
			<span id="download-table-title"></span>
		</h3>
		<p id="download-table-sub-title"></p>
<!--		
		<div id="download-wizard-page-1">
			<h4>Test</h4>
			<div class="slidecontainer">
  				<input type="range" min="1" max="100" value="50" class="slider" id="myRange">
			</div>
			
		
		<table id="download-wizard-page-1" class="selected-pages-table">
			<tr>
				<td class="selected-pages-cell">
					<h4>Left page</h4>
					<div id="download-pages-left" class="unselected-option" onclick="selectPagesClicked('left')">
						<br> <img id="download-image-left" class="download-image">
					</div>
				</td>
				<td class="selected-pages-cell">
					<h4>Right page</h4>
					<div id="download-pages-right" class="unselected-option" onclick="selectPagesClicked('right')">
						<br> <img id="download-image-right" class="download-image">
					</div>
				</td>
				<td class="selected-pages-cell">
					<h4>Range</h4>
					<div id="download-pages-pages" class="selected-option" onclick="selectPagesClicked('pages')">
						<br>
						<div id="download-image-pages"></div>
					</div>
				</td>
			</tr>
		</table>
-->
		
		<table id="download-wizard-page-1" class="selected-pages-table">
			<tr>
				<td class="selected-pages-cell">
					<h4>Left page</h4>
					<div id="download-pages-left" class="unselected-option" onclick="selectPagesClicked('left')">
						<br> <img id="download-image-left" class="download-image">
					</div>
				</td>
				<td class="selected-pages-cell">
					<h4>Right page</h4>
					<div id="download-pages-right" class="unselected-option" onclick="selectPagesClicked('right')">
						<br> <img id="download-image-right" class="download-image">
					</div>
				</td>
				<td class="selected-pages-cell">
					<h4>Range</h4>
					<div id="download-pages-pages" class="selected-option" onclick="selectPagesClicked('pages')">
						<br>
						<div id="download-image-pages"></div>
					</div>
				</td>
			</tr>
		</table>
		
		-->

		<table id="download-wizard-page-2" style="display: none;">
			<tr>
				<td valign="top">
					<h4>Sample</h4>
					<div id="export-musicxml-selected-image"></div>
				<td>
				<td valign="top" style="padding-left: 32px;">
					<h4>Font and languge</h4> <select class="download-select" name="ocr-languages" id="ocr-languages"></select>
					<table class="audiveris-font-table">
						<tr>
							<td colspan="3" align="left">Music font</td>
						</tr>
						<tr>
							<td id="musicfont-bravura" width="33%" align="center" onclick="audiverisMusicFontClicked('musicfont-bravura')"><img src="images/audiveris-images/musicfont-bravura.png"
								class="audiveris-font-image"><br>Bravura</td>
							<td id="musicfont-finale-jazz" width="33%" align="center" onclick="audiverisMusicFontClicked('musicfont-finale-jazz')"><img src="images/audiveris-images/musicfont-finale-jazz.png"
								class="audiveris-font-image"><br>Finale Jazz</td>
							<td id="musicfont-jazz-perc" width="33%" align="center" onclick="audiverisMusicFontClicked('musicfont-jazz-perc')"><img src="images/audiveris-images/musicfont-jazz-perc.png"
								class="audiveris-font-image"><br>Jazz Percussion</td>
						</tr>
					</table>

					<table class="audiveris-font-table">
						<tr>
							<td colspan="3" align="left">Text font</td>
						</tr>
						<tr>
							<td id="textfont-sans-serif" width="33%" align="center" onclick="audiverisTextFontClicked('textfont-sans-serif')"><img src="images/audiveris-images/textfont-sans-serif.png"
								class="audiveris-font-image"><br>Sans Serif</td>
							<td id="textfont-serif" width="33%" align="center" onclick="audiverisTextFontClicked('textfont-serif')"><img src="images/audiveris-images/textfont-serif.png" class="audiveris-font-image"><br>Serif
							</td>
							<td id="textfont-finale-jazz-text" width="33%" align="center" onclick="audiverisTextFontClicked('textfont-finale-jazz-text')"><img
								src="images/audiveris-images/textfont-finale-jazz-text.png" class="audiveris-font-image"><br>Finale Jazz Text</td>
						</tr>
					</table>
				</td>
				<td valign="top" style="padding-left: 32px;">
					<h4>Output</h4> <input type="checkbox" id="export-musicxml-option-lyrics" name="export-musicxml-option-lyrics"> <label for="export-musicxml-option-lyrics">Include lyrics</label><br>
					<input type="checkbox" id="export-musicxml-option-chordnames" name="export-musicxml-option-chordnames"> <label for="export-musicxml-option-chordnames">Include chord names</label><br>
					<input type="checkbox" id="export-musicxml-option-articulations" name="export-musicxml-option-articulations"> <label for="export-musicxml-option-articulations">Include
						articulations</label><br>
					<div style="margin-top: 32px">
						<label class="download-select-label" for="default-instrument">Default instrument</label><br> <select class="download-select" name="default-instrument" id="default-instrument">
							<option selected>Flute</option>
							<option>Organ</option>
							<option>Piano</option>
							<option>Voice</option>
						</select>
					</div>


				</td>
			</tr>
		</table>

		<div class="wizard-buttons-row">
			<button id="download-button-back" disabled onclick="downloadBackClicked();">Back</button>
			<button id="download-button-next" onclick="downloadNextClicked();">Next</button>
			<button id="download-button-cancel" onclick="downloadCancelClicked();">Cancel</button>
		</div>

	</div>

	<div class="modal" id="exportWaitModal" style="display: none;">
		<h3>
			<span id="export-title"></span>
		</h3>
		<div id="percentage-completed"></div>
		<div>
			<button type="button" id="export-download-button" onclick="downloadExportResultClicked();" disabled>Download</button>
			<button type="button" id="export-continue-button" onclick="closeModal();">Continue...</button>
		</div>
	</div>

	<script>
		$(document).ready(pagePageReady());
	</script>
</body>

</html>

