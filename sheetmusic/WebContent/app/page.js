const POST_HISTORY_TIMER_SECONDS = 120;
const CLOSE_MODAL_SECONDS = 10;
const HIDE_NAVIGATION_SECONDS = 10;
const POST_HISTORY_MS = POST_HISTORY_TIMER_SECONDS * 1000;
const CLOSE_MODAL_MS = CLOSE_MODAL_SECONDS * 1000;
const HIDE_NAVIGATION_MS = HIDE_NAVIGATION_SECONDS * 1000;

var id = getRequestParameter("id");
var currentPage = getRequestParameter("page");
var book;
var swiper;
var slidesPerView;
var preferences;
var downloadType;
var downloadWizardStep;
var postHistoryTimer;
var closeModalTimer;
var closeNavigationTimer;
var closePageTitlesTimer;
var modalShown = false;
var cheatSheetModalJustClosed = false;
var closedModalAfterTimeout = false;
var ignoreSwiperSlideChange = false;
var refreshExportMusicXMLStatus;
var exportMusicXMLJobId;

$('#cheat-sheet-modal').on($.modal.BEFORE_CLOSE, function(event, modal) {
	if (closedModalAfterTimeout == false && closedModalAfterTimeout == false) {
		cheatSheetModalJustClosed = true;
	}
});

function pagePageReady() {
	console.log("pagePageReady");
	let url = "users/preferences";
	getJson(url, updateUserPreferences);
}

function updateUserPreferences(data) {
	preferences = data;
	userPreferencesReady();
}

function refreshPage() {
	window.location.replace(window.location.href);
}

function userPreferencesReady() {
	console.log("userPreferencesReady");
	getBook();
	window.addEventListener('keydown', function(event) {
		let key = event.key;
		//console.log("Key pressed with code " + event.keyCode + " and key: " + key);
		if (key === '2') {
			updateSwiper(2);
		}
	});
	addKeyListeners();
	hideNavigation();
	schedulePostHistory();
	updateFavoriteStatus();
}

function isPortrait() {
	return window.innerHeight > window.innerWidth;
}

function initSwiper() {
	let slideTransitionSpeed = 250;
	if (preferences != null) {
		let preferedTransition = preferences.slideTransitionSpeed;
		if (preferedTransition != undefined && preferedTransition > -1) {
			slideTransitionSpeed = preferedTransition;
		}
	}

	swiper = new Swiper('#swiper', {
		slidesPerView: slidesPerView,
		initialSlide: (getRequestParameter("page") - 1),
		spaceBetween: 0,
		centeredSlides: false,
		cssMode: false,
		speed: slideTransitionSpeed,

		navigation: {
			nextEl: '.swiper-button-next',
			prevEl: '.swiper-button-prev',
		},

		observer: true,
		observeParents: true,

		zoom: {
			toggle: true
		}

	});

	swiper.on('slideChangeTransitionStart', function() {
		hideNavigation();
	});

	swiper.on('slideChangeTransitionStart', function() {
		//alert("hatsikidee");
		updatePageLabels();
	});
}

function updateSwiper() {
	initSwiper();
	addSlides();
	loadImagesOnSlide();
	swiper.on('slideChange', swiperSlideChange);
}

function loadImagesOnSlide() {
	let pagesBefore = 1;
	let pagesAfter = 3;
	let currentPage = swiper.realIndex + 1;
	let firstPage = currentPage - pagesBefore;
	if (firstPage < 1) {
		firstPage = 1;
	}
	let lastPage = currentPage + pagesAfter;
	if (lastPage > book.nrOfPages) {
		lastPage = book.nrOfPages;
	}
	for (let page = firstPage; page <= lastPage; page++) {
		let div = "#" + getDivIdForPage(page);
		let src = "page?size=medium&checksum=" + book.fileChecksum + "&page=" + page;
		$(div).attr("src", src);
	}
}

function swiperSlideChange() {
	loadImagesOnSlide();
	updateFavoriteStatus();
}

function addSlides() {
	let initialPage = getRequestParameter("page");
	let slides = [];
	const eagerLoadingSmallSize = false;
	let imageUrlBase = "page?checksum=" + book.fileChecksum + "&aize=small&page=";
	for (let i = 0; i < book.nrOfPages; i++) {
		let html = "<div class='swiper-slide'><div class='swiper-zoom-container'>";
		let page = i + 1;
		let id = getDivIdForPage(page);
		let src = imageUrlBase + page;
		html += "<img class='image-on-slide' id='" + id + "' src='" + src + "'";
		if (eagerLoadingSmallSize) {
			if (i === initialPage || i === (initialPage + 1)) {
				html += " loading='eager'";
			} else {
				html += " loading='lazy'";
			}
		}
		html += "/>";
		html += "</div><div class='swiper-lazy-preloader'></div></div>";
		slides.push(html);
	}
	swiper.appendSlide(slides);
	swiper.update();
}

function getDivIdForPage(page) {
	return "image-page-" + page;
}

function imageUrlToHtml(id, url) {
	let html = "<img class='image-on-slide' id='" + id + "' src='" + url.src + "'/>";
	return html;
}

function getBook() {
	let url = "library/book?id=" + id;
	getJson(url, updateBook);
}

function updateBook(data) {
	book = data;
	initPageDownloadOptions();
	updateOrientation();
}

function updateOrientation() {
	//alert("updateOrientation");
	console.log("updateOrientation");
	slidesPerView = getSlidesPerView();
	if (swiper !== undefined && swiper !== null) {
		swiper.destroy(true, true);
		swiper = undefined;
		setTimeout(() => {
			updateSwiper();
		}, 1000);

	} else {
		updateSwiper();
	}
	updatePageLabels();
}

function updatePageLabels() {
	let pageNr = (swiper.realIndex + 1);
	//console.log("updatePageLabels(" + pageNr + ")");
	let html = "";
	slidesPerView = getSlidesPerView();
	for (let i = 0; i < slidesPerView; i++) {
		let pageIndex = pageNr + i;
		let title = getPageTitle(pageIndex);
		html += "<div>";
		if (title !== "") {
			html += "<span class='page-title'>" + title + "</span>";
		}
		html += "</div>";
	}
	//console.log("page container: " + html);
	$("#page-title-container").html(html);

	if (closePageTitlesTimer != undefined) {
		clearTimeout(closePageTitlesTimer);
	}
	$("#page-title-container").show();
	closePageTitlesTimer = setTimeout(hidePageTitles, HIDE_NAVIGATION_MS);
}

function getPageTitle(pageNr) {
	let foundPage;
	for (let i = 0; i < book.pages.length; i++) {
		let current = book.pages[i];
		if (current.nr === pageNr) {
			foundPage = current;
		}
	}
	let title;
	if (foundPage == undefined) {
		title = "";
	} else {
		title = foundPage.label;
	}
	return title;
}

function hidePageTitles() {
	$("#page-title-container").hide();
}

function getSlidesPerView() {
	let newSlidesPerView;
	if (usingMobileDevice()) {
		if (isPortrait()) {
			newSlidesPerView = 1;
		} else {
			newSlidesPerView = 2;
		}
	} else {
		newSlidesPerView = preferences.slidesPerView;
	}
	return newSlidesPerView;
}

// ======================================================
// Download and export
// ======================================================

function downloadClicked(type) {
	downloadType = type;
	downloadWizardStep = 1;
	let startPage = (swiper.realIndex + 1);
	let endPage = startPage + 1;

	$('#download-select-from').val(startPage);
	$('#download-select-to').val(endPage);

	$('#download-image-left').attr("src", getImageUrl(startPage));
	$('#download-image-right').attr("src", getImageUrl(endPage));

	let title;

	if (downloadType === 'musicxml') {
		title = "Listen to the music";
		$('#export-musicxml-options').show();
	} else {
		title = "Download pages";
		$('#export-musicxml-options').hide();
	}
	$('#download-table-title').html(title);
	showDownloadWizardPage();
	if ($('#ocr-language option').length == 0) {
		getJson("ocr-languages", addLanguageOptions);
	}
}

function addLanguageOptions(data) {
	let languages = data;
	if (languages != undefined) {
		for (let i = 0; i < languages.length; i++) {
			let language = languages[i];
			$('#ocr-languages').append(new Option(language.language, language.code));
		}
	}
	$('#downloadModal').modal();
	modalShown = true;
	updateDownloadOptionsFromPreferences();
}



function updateDownloadOptionsFromPreferences() {
	let ocrSettings = preferences.ocr.preferences;
	console.log(ocrSettings);

	let language = getOmrPreference("org.audiveris.omr.text.Language.defaultSpecification");
	$("select#ocr-languages").val(language);

	let textFont = getOmrPreference("org.audiveris.omr.ui.symbol.TextFont.defaultTextFamily");
	if (textFont === "SansSerif") {
		audiverisTextFontClicked('textfont-sans-serif');
	} else {
		if (textFont === "Serif") {
			audiverisTextFontClicked('textfont-serif');
		} else {
			if (textFont == "FinaleJazzText") {
				audiverisTextFontClicked('textfont-finale-jazz-text');
			}
		}
	}
	let musicFont = getOmrPreference("org.audiveris.omr.ui.symbol.MusicFont.defaultMusicFamily");
	if (musicFont === "Bravura") {
		audiverisMusicFontClicked('musicfont-bravura');
	} else {
		if (musicFont === "FinaleJazz") {
			audiverisMusicFontClicked('musicfont-finale-jazz')
		} else {
			if (musicFont === "JazzPerc") {
				audiverisMusicFontClicked('musicfont-jazz-perc');
			}
		}
	}
	let lyrics = getOmrPreference("org.audiveris.omr.sheet.ProcessingSwitches.lyrics");
	if (lyrics) {
		$('#export-musicxml-option-lyrics').prop("checked");
	}

	let chordNames = getOmrPreference("org.audiveris.omr.sheet.ProcessingSwitches.chordNames");
	if (chordNames) {
		$('#export-musicxml-option-chordnames').prop("checked");
	}

	let articulations = getOmrPreference("org.audiveris.omr.sheet.ProcessingSwitches.articulations");
	if (articulations) {
		$('#export-musicxml-option-articulations').prop("checked");
	}

	let instrument = getOmrPreference("org.audiveris.omr.score.LogicalPart.defaultSingleStaffPartName");

	$("select#default-instrument").val(instrument);
}

function getOmrPreference(name) {
	let result;
	let omrPrefs = preferences.ocr.preferences;
	for (let i = 0; i < omrPrefs.length; i++) {
		let pref = omrPrefs[i];
		if (pref.name === name) {
			result = pref.value;
		}
	}
	return result;
}

function selectPagesClicked(type) {
	$('#download-pages-left').removeClass("selected-option");
	$('#download-pages-right').removeClass("selected-option");
	$('#download-pages-pages').removeClass("selected-option");
	$('#download-pages-left').addClass("unselected-option");
	$('#download-pages-right').addClass("unselected-option");
	$('#download-pages-pages').addClass("unselected-option");
	let selectedOption = "#download-pages-" + type;
	$(selectedOption).removeClass("unselected-option");
	$(selectedOption).addClass("selected-option");
}

function getImageUrl(page) {
	return 'page?checksum=' + book.fileChecksum + '&page=' + page + '&size=medium';
}

function downloadBackClicked() {
	downloadWizardStep = 1;
	showDownloadWizardPage();
}

function downloadNextClicked() {
	console.log("downloadNextClicked " + downloadType);
	if (downloadType === 'sheets') {
		download();
	} else {
		if (downloadWizardStep == 1) {
			downloadWizardStep = 2;
			console.log("Going to step 2");
			let pages = getSelectedPages();
			let from = -1;
			if (pages === 'left') {
				let leftPage = (swiper.realIndex + 1);
				from = leftPage;
			} else {
				if (pages === 'right') {
					let to = (swiper.realIndex + 2);
					from = to;
				} else {
					if (pages === 'pages') {
						from = parseInt($('#download-select-from').val());
					}
				}
			}
			let imageSrc = getImageUrl(from);
			let backgroundImageUrl = "url('" + imageSrc + ")'";

			$("#export-musicxml-selected-image").css("background-image", backgroundImageUrl);
			showDownloadWizardPage();
		} else {
			if (downloadWizardStep == 2) {
				download();
			}
		}
	}
}

function showDownloadWizardPage() {
	if (downloadWizardStep == 1) {
		$("#download-wizard-page-1").show();
		$("#download-wizard-page-2").hide();
		$("#download-button-back").prop("disabled", true);
		let subtitle;
		if (downloadType === 'musicxml') {
			subtitle = "Listen to the music in applications like <a href='https://musescore.org/'>MuseScore</a> by exporting pages to the <a href='https://www.musicxml.com/'>MusicXML</a>, the standard open format for exchanging digital sheet music. Use these files in MuseScore for listening, or conver them to MIDI for performing on electric piano and keyboard.";
		} else {
			subtitle = "Download a copy of the muitple pages as PDF file, or single page as JPG.";
		}
		$('#download-table-sub-title').html(subtitle);
	}
	if (downloadWizardStep == 2) {
		$("#download-wizard-page-1").hide();
		$("#download-wizard-page-2").show();
		$("#download-button-back").prop("disabled", false);
		let subtitle;
		if (downloadType === 'musicxml') {
			subtitle = "<p>Click below to download the MusicXML files for usage in applications like MuseScore.</p><p>The pages will be converted by <a href='https://github.com/Audiveris/audiveris'>AudiVeris</a>, the open source Optical Music Recognition (OMR) software. Audiveris will scan the sheet music and convert it into a machine-readable format, such as MusicXML or MIDI. This allows the music to be edited, played back, and shared digitally.</p><p>Make a selection on the options below for the best scanning result. More information on these options can be found in the <a href='https://audiveris.github.io/audiveris/_pages/handbook/'>Audiveris handbook.</a></p>";
		} else {
			subtitle = "Download a copy of the pages as PDF file";
		}
		$('#download-table-sub-title').html(subtitle);
	}
}

function getSelectedPages() {
	let result;
	if ($('#download-pages-left').hasClass('selected-option')) {
		result = "left";
	} else {
		if ($('#download-pages-right').hasClass('selected-option')) {
			result = "right";
		} else {
			if ($('#download-pages-pages').hasClass('selected-option')) {
				result = "pages";
			}
		}
	}
	return result;
}

function downloadCancelClicked() {
	closeModal();
}

function audiverisTextFontClicked(fontName) {
	console.log("Text font: " + fontName);

	$('#textfont-sans-serif').removeClass("selected-option");
	$('#textfont-serif').removeClass("selected-option");
	$('#textfont-finale-jazz-text').removeClass("selected-option");

	$('#' + fontName).addClass("selected-option");
}

function audiverisMusicFontClicked(fontName) {
	$('#musicfont-bravura').removeClass("selected-option");
	$('#musicfont-finale-jazz').removeClass("selected-option");
	$('#musicfont-jazz-perc').removeClass("selected-option");
	$('#' + fontName).addClass("selected-option");
	console.log("Music font: " + fontName);
}


function download() {
	let type = getSelectedPages();
	let from = -1;
	let to = -1;
	if (type === 'left') {
		let leftPage = swiper.realIndex + 1;
		from = leftPage;
		to = leftPage;
	} else {
		if (type === 'right') {
			to = (swiper.realIndex + 2);
			from = to;
		} else {
			if (type === 'pages') {
				from = parseInt($('#download-select-from').val());
				to = parseInt($('#download-select-to').val());
			}
		}
	}
	if (from > -1) {
		if (to >= from) {
			$.modal.close();
			let musicxml = (downloadType === 'musicxml');
			if (musicxml) {
				startMusicXMLJob(from, to);
			} else {
				let id = book.fileChecksum;
				let location = "download/" + id + "?from=" + from + "&to=" + to;
				window.location = location;
			}
		} else {
			console.log("Invalid page range from " + from + " to " + to);
		}
	}
}

function startMusicXMLJob(from, to) {

	let jobData = {};
	jobData.bookId = book.fileChecksum;
	jobData.from = from;
	jobData.to = to;
	let options = [];

	let language = $('#ocr-languages').val();
	options.push(getOption("org.audiveris.omr.text.Language.defaultSpecification", language));

	// Bravura, FinaleJazz, JazzPerc
	let musicfont = "";
	if ($('#musicfont-bravura').hasClass('selected-option') == true) {
		musicfont = "Bravura";
	}
	if ($('#musicfont-finale-jazz').hasClass('selected-option')) {
		musicfont = "FinaleJazz";
	}
	if ($('#musicfont-jazz-perc').hasClass('selected-option')) {
		musicfont = "JazzPerc";
	}
	if (musicfont !== "") {
		options.push(getOption("org.audiveris.omr.ui.symbol.MusicFont.defaultMusicFamily", musicfont));
	}

	let textfont = "";
	// SansSerif, Serif,  FinaleJazzText
	if ($('#textfont-sans-serif').hasClass('selected-option')) {
		textfont = "SansSerif";
	}
	if ($('#textfont-serif').hasClass('selected-option')) {
		textfont = "Serif";
	}
	if ($('#textfont-finale-jazz-text').hasClass('selected-option')) {
		textfont = "FinaleJazzText";
	}
	console.log("textfont: " + textfont);
	if (textfont !== "") {
		options.push(getOption("org.audiveris.omr.ui.symbol.TextFont.defaultTextFamily", textfont));
	}

	let chordNames = $('#export-musicxml-option-chordnames').is(":checked");
	console.log(chordNames);
	options.push(getOption("org.audiveris.omr.sheet.ProcessingSwitches.chordNames", chordNames));

	let lyrics = $('#export-musicxml-option-lyrics').is(":checked");
	options.push(getOption("org.audiveris.omr.sheet.ProcessingSwitches.lyrics", lyrics));

	let articulations = $('#export-musicxml-option-articulations').is(":checked");
	options.push(getOption("org.audiveris.omr.sheet.ProcessingSwitches.articulations", articulations));

	let instrument = $('#default-instrument').val();
	options.push(getOption("org.audiveris.omr.score.LogicalPart.defaultSingleStaffPartName", instrument));


	jobData.options = options;
	postJson("download-musicxml", jobData, receiveJobId);
}

function getOption(name, value) {
	let option = {};
	option.name = name;
	option.value = value;
	return option;
}

function receiveJobId(data) {
	exportMusicXMLJobId = data;
	console.log(exportMusicXMLJobId);
	getJobStatus();
	refreshExportMusicXMLStatus = setInterval(getJobStatus, 1000);
	$('#exportWaitModal').modal();
}

function getJobStatus() {
	let url = "tasks-musicxml?id=" + exportMusicXMLJobId;
	getJson(url, receiveJobStatus);
}

function receiveJobStatus(job) {
	updateStatus(job);
}

function updateStatus(job) {
	let exporttitle = "Exporting music";
	let completed = job.percentageCompleted;
	$('#percentage-completed').html("<p>Completed: " + completed + "%</p>");
	if (job.status === "Ready for download" || job.status === "Error") {
		$('#percentage-completed').html("");
		if (job.status === "Ready for download") {
			$('#export-download-button').prop('disabled', false);
			exporttitle = "Done";
		} else {
			exporttitle = "Sorry, this sheet could not be exported";
		}
		$('#export-continue-button').hide();
		clearInterval(refreshExportMusicXMLStatus);
	}
	$('#export-title').html(exporttitle);
}

function updateStatusOld(job) {
	$('#export-book-label').html(job.book.label);
	let pages = "All pages";
	let from = job.from;
	let to = job.to;
	if (from != undefined && from > 0) {
		pages = from;
		if (to != undefined && to > 0) {
			pages = pages + " - " + to;
		}
	}
	$('#export-book-pages').html(pages);
	let since = getTimeAgo(job.startTime) + ", " + formatDateTime(job.startTime);
	$('#export-book-started').html(since);

	let status = job.status;
	let step = job.step;
	let completed = job.percentageCompleted;
	if (step != undefined && step != "") {
		status = status + " - " + step;
	}
	$('#export-book-status').html(status);

	if (job.status === "Ready for download" || job.status === "Error") {
		if (job.status === "Ready for download") {
			$('#export-download-button').prop('disabled', false);
		}
		$('#export-continue-button').hide();
		clearInterval(refreshExportMusicXMLStatus);
	}
}

function downloadExportResultClicked() {
	let location = "download-job?id=" + exportMusicXMLJobId;
	window.location = location;
	closeModal();
}

function initPageDownloadOptions() {
	let html = "";
	html = html + '<label for="download-select-from">From </label>';
	html = html + '<select name="download-select-from" id="download-select-from">';
	html = html + generatePageOptions();
	html = html + '</select>';
	html = html + '<label for="download-select-to">&nbsp;to&nbsp;</label>';
	html = html + '<select name="download-select-to" id="download-select-to">';
	html = html + generatePageOptions();
	html = html + '</select>';
	$('#download-pages-pages').html(html);
}

function generatePageOptions() {
	let html = "";
	for (let i = 1; i <= book.nrOfPages; i++) {
		html = html + generatePageOption(i);
	}
	return html;
}

function generatePageOption(page) {
	let html = '<option value="' + page + '">' + page + '</option>';
	return html;
}

function addPageThumbnail(page) {
	return '<img class="download-image" src="page?checksum=' + book.fileChecksum + '&page=' + page + '&size=small">';
}

// ================================================================================================================
// 
// ================================================================================================================

function openBookClicked() {
	let url = 'book.jsp?id=' + book.fileChecksum;
	location.href = url;
}

function clickBookImage(event) {
	//alert("clickBookImage");
	const TOP_AND_BOTTOM = 50;
	var x = event.clientX;
	var y = event.clientY;
	var winWidth = window.innerWidth; // - MARGIN_AROUND_IMAGE;
	var winHeight = window.innerHeight; // - MARGIN_AROUND_IMAGE;
	var widthMiddle = winWidth / 2;
	var widthMiddleLeft = widthMiddle / 2;
	var widthMiddleRight = widthMiddle + widthMiddleLeft;
	var location = "";

	if (x < widthMiddleLeft) {
		location = location + "left";
	} else {
		if (x > widthMiddleRight) {
			location = location + "right";
		} else {
			location = "middle";
		}
	}

	if (y < TOP_AND_BOTTOM) {
		location = location + "-top"
	} else {
		if (y > winHeight - TOP_AND_BOTTOM) {
			location = location + "-bottom";
		} else {
			location = location + "-middle"
		}
	}
	// alert("click " + location + " " + coords);
	handleClickImage(location);
}

function handleClickImage(location) {
	//alert("handleClickImage(location: " + location + ", favoriteModelIsShown " + modalShown);
	if (modalShown == false) {
		if (location === "right-top") {
			handleCloseAction();
		} else {
			if (location === "left-middle") {
				previousPage();
			} else {
				if (location === "right-middle") {
					nextPage();
				} else {
					if (location === "middle-bottom") {
						nextSinglePage();
					} else {
						if (location === "middle-top") {
							previousSinglePage();
						} else {
							toggleNavigation();
						}
					}
				}
			}
		}
	}
}

function addKeyListeners() {
	document.body.addEventListener("click", function(event) {
		toggleNavigation();
	});

	$("#swiper").click(clickBookImage);

	window.addEventListener("orientationchange", function(event) {
		updateOrientation();
	});

	window.addEventListener('keydown', function(event) {
		console.log("Key pressed with code " + event.keyCode);

		if ($.modal.isActive()) {
			console.log("Modal is open");
		} else {

			let handled = false;
			switch (event.keyCode) {
				case 175: {
					// volume up on logitech keyboard
					handleCloseAction();
					handled = true;
					break;
				}
				case 189: {
					// - minus
					handleCloseAction();
					handled = true;
					break;
				}
				case 173: {
					// minus op desktop keyboard
					handleCloseAction();
					handled = true;
					break;
				}
				case 27: {
					// esc
					if (cheatSheetModalJustClosed === true) {
						cheatSheetModalJustClosed = false;
					} else {
						handleCloseAction();
					}
					handled = true;
					break;
				}
				case 37: {
					// left key
					left();
					handled = true;
					break;
				}
				case 39: {
					// right key
					right();
					handled = true;
					break;
				}
				case 38: {
					// up key pressed
					up();
					handled = true;
					break;
				}
				case 40: {
					// down key pressed
					down();
					handled = true;
					break;
				}
				case 34: {
					// page down pressed
					right();
					handled = true;
					break;
				}
				case 33: {
					// page up pressed
					left();
					handled = true;
					break;
				}
			}

			if (handled === false) {
				toggleNavigation();
				handleKey(event.key);
			}
		}
	});
}

function handleKey(key) {
	console.log("handleKey(" + key + ")");

	let isSlidesPerViewKey = (key == 1 || key == 2 || key == 3 || key == 4);
	let isCheatSheetKey = (key == 5 || key == 6 || key == 7 || key == 8 || key == 9);

	if (isSlidesPerViewKey) {
		handleChangeSlidesPerView(key);
	} else {
		if (isCheatSheetKey) {
			let imageURL = "images/cheat-sheets/" + key + ".png";
			let newHTML = '<img src="' + imageURL + '">';
			let modal = $("#cheat-sheet-modal");
			modal.html(newHTML);
			modal.modal();
			scheduleCloseModal();
			closedModalAfterTimeout = false;
			cheatSheetModalOpen = true;
		}
	}
}

function handleChangeSlidesPerView(newSlidesPerView) {
	slidesPerView = newSlidesPerView;
	let preferences = {};
	preferences.slidesPerView = newSlidesPerView;
	postJson("users/preferences", preferences, updateSwiper);
}

function handleCloseAction() {
	if (!$.modal.isActive()) {
		window.history.back();
	}
}

function up() {
	previousPage();
}

function down() {
	nextPage();
}

function left() {
	previousSinglePage();
}

function right() {
	nextSinglePage();
}

function nextSinglePage() {
	swiper.slideNext();
}

function previousSinglePage() {
	swiper.slidePrev();
}

function nextPage() {
	let newIndex = swiper.realIndex + slidesPerView;
	swiper.slideTo(newIndex);
}

function previousPage() {
	let newIndex = swiper.realIndex - slidesPerView;
	swiper.slideTo(newIndex);
}

function toggleNavigation() {
	$("#swiper-right-top-menu").show();
	$("#swiper-left-top-menu").show();
	scheduleHideNavigation();
}

function scheduleHideNavigation() {
	if (closeNavigationTimer != undefined) {
		clearTimeout(closeNavigationTimer);
	}
	closeNavigationTimer = setTimeout(hideNavigation, HIDE_NAVIGATION_MS);
}

function scheduleCloseModal() {
	if (closeModalTimer != undefined) {
		clearTimeout(closeModalTimer);
	}
	closeModalTimer = setTimeout(closeModal, CLOSE_MODAL_MS);
}

function closeModal() {
	closedModalAfterTimeout = true;
	clearInterval(refreshExportMusicXMLStatus);
	$.modal.close();
}

function hideNavigation() {
	$("#swiper-right-top-menu").hide();
	$("#swiper-left-top-menu").hide();
}

function pageIndex(page, pages) {
	let result = -1;
	let i = 0;
	for (const p of pages) {
		if (p === page) {
			result = i;
		}
		i++;
	}
	return result;
}

function refreshOrientation() {
	let orientation = getScreenOrientation();
	if (orientation === 'portrait') {
		slidesArray = slidesPortrait;
		pageNumberArray = slidesPortraitPages;
	} else {
		let currentPage = (swiper.realIndex + 1);
		if (currentPage % 2 == 0) {
			slidesArray = slidesLandscapeEven;
			pageNumberArray = slidesLandscapeEvenPages;
		} else {
			slidesArray = slidesLandscapeUneven;
			pageNumberArray = slidesLandscapeUnevenPages;
		}
	}
	ignoreSwiperSlideChange = true;
	swiper.removeAllSlides();
	swiper.appendSlide(slidesArray);
	ignoreSwiperSlideChange = false;
}

// ==================
// F A V O R I T E S
// ==================

var currentUserLabel = '';
var currentPageFavorite;
var favoriteRequest;

function updateFavoriteStatus() {
	console.log("updateFavoriteStatus");
	let pagenumber;
	if (swiper == undefined) {
		pagenumber = currentPage;
	} else {
		pagenumber = swiper.realIndex + 1;
	}
	let url = "rest/favorites/page/";
	url += id;
	url += "/" + pagenumber;
	getJson(url, updateFavoriteStatusFromAjax);
}

function updateFavoriteStatusFromAjax(data) {
	//console.log("updateFavoriteStatusFromAjax");
	//console.log(data);
	favoriteRequest = data;
	let isFavorite = data.favorite;
	currentUserLabel = "";
	if (isFavorite) {
		currentPageFavorite = true;
		let userLabel = data.userLabel;
		if (userLabel != undefined) {
			currentUserLabel = userLabel;
		}
	} else {
		currentPageFavorite = false;
	}
	updateFavoriteIcon(currentPageFavorite);
}

function favoritesPageClicked(yesOrNo) {
	$("#favoriteFormFileName").html(book.label);
	$("#favoriteFormStartPage").html((swiper.realIndex + 1));

	if (yesOrNo == 'yes') {
		$("#favoriteFormUserLabel").val(currentUserLabel);
		$("#favoriteTitle").html('Change favorite');
		$("#favoritesModalDelete").show();
	} else {
		let userLabel = book.label;
		if (favoriteRequest != undefined) {
			let pageLabel = favoriteRequest.pageLabel;
			if (pageLabel != undefined && pageLabel != "") {
				userLabel = pageLabel;
			}
		}
		$("#favoriteFormUserLabel").val(userLabel);
		$("#favoriteTitle").html('Add favorite');
		$("#favoritesModalDelete").hide();
	}

	$('#favoriteModal').modal();
	modalShown = true;
}

function handleFavoriteFormUserLabelClear() {
	$("#favoriteFormUserLabel").val("");
}

function favoritesModalOk() {
	hideFavoritesModal();
	let id = book.fileChecksum;
	let pagenumber = (swiper.realIndex + 1);
	var data = {};
	data.userLabel = $("#favoriteFormUserLabel").val();
	let url = "rest/favorites/page/";
	url += id;
	url += "/" + pagenumber;
	postJson(url, data, updateFavoriteStatus);
	hideFavoritesModal();
}

function favoritesModalDelete() {
	hideFavoritesModal();
	let id = book.fileChecksum;
	let pagenumber = (swiper.realIndex + 1);
	let url = "rest/favorites/page/" + id + "/" + pagenumber;
	deleteJson(url, updateFavoriteStatus);
	hideFavoritesModal();
}

function favoritesModalCancel() {
	hideFavoritesModal();
}

function hideFavoritesModal() {
	modalShown = false;
	closeModal();
}

// ==================
// H I S T O R Y
// ==================

function schedulePostHistory() {
	if (postHistoryTimer != undefined) {
		clearTimeout(postHistoryTimer);
	}
	postHistoryTimer = setTimeout(postHistory, POST_HISTORY_MS);
}

function postHistory() {
	var data = {};
	data.id = book.fileChecksum;
	data.page = (swiper.realIndex + 1);
	postJson("history", data, historyPostCompleted);
}

function historyPostCompleted() {
	console.log("History posted");
}

function openFullscreen() {
	let elem = document.documentElement;
	if (elem.requestFullscreen) {
		elem.requestFullscreen();
	} else if (elem.webkitRequestFullscreen) { /* Safari */
		elem.webkitRequestFullscreen();
	} else if (elem.msRequestFullscreen) { /* IE11 */
		elem.msRequestFullscreen();
	}
}

function closeFullscreen() {
	if (document.exitFullscreen) {
		document.exitFullscreen();
	} else if (document.webkitExitFullscreen) { /* Safari */
		document.webkitExitFullscreen();
	} else if (document.msExitFullscreen) { /* IE11 */
		document.msExitFullscreen();
	}
}

function usingMobileDevice() {
	const userAgent = navigator.userAgent.toLowerCase();
	let isMobile = /iPhone|Android/i.test(navigator.userAgent);
	let isTablet = /(ipad|tablet|(android(?!.*mobile))|(windows(?!.*phone)(.*touch))|kindle|playbook|silk|(puffin(?!.*(IP|AP|WP))))/.test(userAgent);
	//console.log(userAgent + " isMobile: " + isMobile + "isTablet:" +  isTablet);
	return isTablet || isMobile;
}

function addLeadingZero(i) {
	if (i < 10) {
		i = "0" + i;
	}  // add zero in front of numbers < 10
	return i;
}