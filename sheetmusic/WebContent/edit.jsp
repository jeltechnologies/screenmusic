<!DOCTYPE html>
<%@page import="com.jeltechnologies.screenmusic.servlet.ScreenMusicContext"%>
<%@ taglib prefix="screenmusic" uri="WEB-INF/tags.tld"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="com.jeltechnologies.utils.JsonUtils"%>
<%@page import="com.jeltechnologies.screenmusic.library.Category"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="com.jeltechnologies.screenmusic.tags.BaseTag"%>
<%@page import="com.jeltechnologies.utils.StringUtils"%>
<%@page import="com.jeltechnologies.screenmusic.library.BookPage"%>
<%@page import="com.jeltechnologies.screenmusic.library.Book"%>
<%@page import="com.jeltechnologies.screenmusic.library.Library"%>
<%@page import="com.jeltechnologies.screenmusic.User"%>
<%@page import="com.jeltechnologies.screenmusic.servlet.BaseServlet"%>
<%!

List<String> textFieldIds = new ArrayList<String>();
List<String> pageTitleIds = new ArrayList<String>();
List<String> pageArtistIds = new ArrayList<String>();

public String addInputText(String inputId, String name, String value, boolean hidden) {
	return addInputText(inputId, name, value, hidden, false, false, null, null);
}

public String addInputText(String inputId, String name, String value, boolean hidden, boolean multipleLines) {
    return addInputText(inputId, name, value, hidden, multipleLines, false, null, null);
}

public String addInputText(String inputId, String name, String value, boolean hidden, boolean multipleLines, boolean readOnly) {
    return addInputText(inputId, name, value, hidden, multipleLines, readOnly, null, null);
}

public String addInputText(String inputId, String name, String value, boolean hidden, boolean multipleLines, boolean readOnly, String button, String buttonScript) {
    textFieldIds.add(inputId);
    StringBuilder b = new StringBuilder(); 
    String labelId = inputId + "_div";
    String displayValue;
    if (value == null) {
		displayValue = "";
    }
    else {
		displayValue = value;
    }
	b.append("<label for=\"").append(inputId).append("\">").append(name);
	if (button != null) {
	    b.append("<button class=\"button-small\" type=\"button\" onclick=\"");
	    b.append(buttonScript).append("\";return false;>").append(button);
	    b.append("</button>");
	}
	b.append("</label>");
	int columns;
	if (button == null) {
	    columns = 40;
	} else {
	    columns = 20;
	}
	if (multipleLines) {
	    b.append("<textarea id=\"").append(inputId);
	    b.append("\" name=\"").append(inputId).append("\" rows=\"20\" cols=\"").append(columns).append("\"");
	    if (readOnly) {
			b.append(" readonly");
	    }
	    b.append(">");
	    b.append(value);
		b.append("</textarea>");
	} else {
		b.append("<input type=\"text\" id=\"");
		b.append(inputId).append("\" name=\"");
		b.append(inputId).append("\" maxlength=\"255\" size=\"").append(columns).append("\"");
		if (readOnly) {
			b.append(" readonly");
		}
		b.append(" value=\"");
		b.append(displayValue).append("\">");
	}
	return b.toString();
}

%>
<%
	String bookId = (String) request.getParameter("id");
	ScreenMusicContext screenmusicContext = new ScreenMusicContext(application);
	User user = BaseServlet.getUser(request);
	Library library = new Library(user, screenmusicContext);
	Book book = library.getBookWithCategories(bookId);
	List<String> fileNames = library.getBookFiles(book);
	String fileName = fileNames.get(0);
	
	String fileEncoded = StringUtils.encodeURL(fileName);
	String bookTitle = StringUtils.getValueEmptyIfNull(book.getTitle());
	String bookArtist = StringUtils.getValueEmptyIfNull(book.getArtist());
	
	java.io.File file = user.getFile(fileName);
	String bookFileName = file.getName();
	String folderName = StringUtils.stripAfterLast(fileName, "/");
	String allCategoriesJson = new JsonUtils().toJSON(library.getAllCategories());
%>

<html>
<head>
<jsp:include page="head.jsp"></jsp:include>

<style>
input[type=text], select, textarea {
	width: 100%;
	padding: 12px 8px;
	margin: 8px 0;
	display: block;
	border: 1px solid;
	border-color: var(- -dark);
	border-radius: 4px;
	box-sizing: border-box;
}

input[type=submit] {
	width: 100%;
	background-color: var(- -background);
	color: white;
	padding: 14px 20px;
	margin: 8px 0;
	border: none;
	border-radius: 4px;
	cursor: pointer;
}

input:read-only {
	border: none;
	background-color: transparent;
	resize: none;
	outline: none;
}

button {
	width: 80px;
	background-color: var(- -pale);
	color: var(- -foreground);
}

button[type=submit] {
	background-color: var(- -accent);
	color: var(- -background);
}

div.container {
	border-radius: 5px;
	background-color: var(- -grey);
	padding-left: 20px;
	padding-right: 20px;
}

.button-small {
	width: 20px;
	background: var(- -pale);
	color: var(- -foreground);
	font-size: xx-small;
	height: 16px;
	margin: 8px;
	display: inline-flex;
	align-items: center;
	justify-content: center;
}

.editImage {
	float: left;
	background-color: var(- -background);
	margin: 16px;
}

.editImage img {
	max-height: 600px;
	max-width: 600px;
}

.editFields {
	float: left;
	margin: 16px;
}

.bookManagementButton {
	width: 160px;
	background-color: var(- -pale);
	color: var(- -foreground);
}

.category {
	display: flex;
}

.select-category {
	float: left;
}

.select-category-button {
	float: left;
	margin: 16px;
	padding: 0px 0px 0px 0px;
	font-size: x-small;
	height: 24px;
	background-color: var(- -pale);
	color: var(- -foreground);
}
</style>
<script>

var nrOfCategories;
var selectedCategories = [];
var categories = [];

function initCategories() {
	nrOfCategories = 0;
	categories = [];
	let bookCategories = <%=new JsonUtils().toJSON(book.getCategories())%>;
	let allCategories = <%=allCategoriesJson%>;
	for (let i=0; i< allCategories.length; i++) {
		let category = allCategories[i].name;
		categories.push(category);
	}
	for (let i=0; i< bookCategories.length; i++) {
		let category = bookCategories[i].name;
		selectedCategories.push(category);
	}	
	updateCategoryHtml();
}

function updateCategoryHtml() {
	//console.log("updateCategoryHtml");
	let html = "";
	for (let i=0; i< selectedCategories.length; i++) {
		//html = html + generateCategoryHtml(i + 1, selectedCategories[i]);
		html = html + generateTextField((i + 1), selectedCategories[i]);
	}
	html = html + generateCategoryHtml(0, "");
	$("#categories").html(html);
}

function generateTextField(nr, selected) {
	let nameAndId = "category_" + nr;
	let html = "<div class='category'><input id='" + nameAndId + "' name='" + nameAndId + "' type='text' value='" + selected + "' readonly>";
	html = html + generateRemoveCategorySelectButton(nameAndId, nr);
	html = html + "</div>";
	return html;
}

function generateCategoryHtml(nr, selected) {
	let nameAndId = "category_" + nr;
	let html = "<div class='category'><select name=\"" + nameAndId + "\" id=\"" + nameAndId + "\" class='select-category' onchange=\"selectedCategory('" + nameAndId + "', this);\">";
	html = html  + generateSelectOption("", selected);
	for (let i = 0;i< categories.length;i++) {
		let category = categories[i];
		
		let categoryAlreadySelected = false;
		for (let c=0;c<selectedCategories.length && !categoryAlreadySelected;c++) {
			if (selectedCategories[c] === category) {
				categoryAlreadySelected = true;
			}
		}
		if (!categoryAlreadySelected) {
			html = html  + generateSelectOption(category, selected);
		}
	}
	html = html + "</select>";
	if (nr > 0 ) {
		html = html + generateRemoveCategorySelectButton(nameAndId, nr);
	}
	html = html + "</div>";
	return html;
}

function generateSelectOption(option, selected) {
	let html = "<option value='" + option + "'";
	if (option === selected) {
		html = html + " selected";
	}
	html = html + ">" + option + "</option>";
	return html;
}

function generateRemoveCategorySelectButton(id, nr) {
	let buttonId = id + "-remove-button";
	let html = "<button id='" + buttonId + "' class='select-category-button'";
	html = html + " onclick=\"removeCategory(" + nr + ")\">Remove" + "</button>";
	return html;
}

function selectedCategory(id, select) {
	let value = select.value;
	addCategory(value);
}

function addCategory(lastCategory) {
	selectedCategories.push(lastCategory);
	updateCategoryHtml();
}

function removeCategory(nr) {
	console.log("removeCategory(" + nr + ")");
	let newCategories = [];
	for (let i=0;i<selectedCategories.length;i++) {
		let removeNr = i + 1;
		if (removeNr != nr) {
			newCategories.push(selectedCategories[i]);
		}
	}
	selectedCategories = newCategories;
	updateCategoryHtml();
}

function getBookUpdate() {
	let bookUpdate = {};
	
	bookUpdate.fileChecksum = getUrlParameter("id");
	
	bookUpdate.title = $("#txtBookTitle").val();
	bookUpdate.artist = $("#txtBookArtist").val();
	bookUpdate.series = $("#txtBookSeries").val();
	
	bookUpdate.categories = [];
	for (let i=0;i<selectedCategories.length;i++) {
		let bookCategory = {};
		bookCategory.name = selectedCategories[i];
		bookUpdate.categories[i] = bookCategory;
	}

	let editedPages = [];
	let arrayCounter = 0;

	for (page = 1; page <= <%=book.getNrOfPages()%>; page++) {
		let txtBoxTitleId = "txtPageTitle_" + page;
		let title = $("#" + txtBoxTitleId).val().trim();

		let txtArtistId = "txtPageArtist_" + page;
		let artist = $("#" + txtArtistId).val().trim();

		let txtBoxTitle2Id = "txtPageTitle2_" + page;
		let title2 = $("#" + txtBoxTitle2Id).val().trim();

		let txtArtist2Id = "txtPageArtist2_" + page;
		let artist2 = $("#" + txtArtist2Id).val().trim();

		let txtBoxDescriptionId = "txtPageText_" + page;
		let text = $("#" + txtBoxDescriptionId).val().trim();
		
		editedPages[arrayCounter] = {};

		if (LOGGING) {
			console.log("Nr: " + nr);
			console.log("Title: " + title);
			console.log("Artist: " + artist);
			console.log("Title2: " + title2);
			console.log("Artist2: " + artist2);
			console.log("Notes: " + text);
		}

		editedPages[arrayCounter].nr = page;
		editedPages[arrayCounter].title = title;
		editedPages[arrayCounter].artist = artist;
		editedPages[arrayCounter].title2 = title2;
		editedPages[arrayCounter].artist2 = artist2;
		editedPages[arrayCounter].text = text;
			
		arrayCounter++;		
	}
	bookUpdate.pages = editedPages;
	return bookUpdate;
}

function doPostUpdate() {
	let bookUpdate = getBookUpdate();
	let operation = {};
	operation.operation = "update";
	operation.book = bookUpdate;
	postJson("library/book", operation, postEditChangeCompleted);
}

function postEditChangeCompleted(data) {
	console.log(data);
	let url = "book.jsp?id=" + data.fileChecksum;
	window.location.replace(url);
}

function moveClicked() {
	let modal = $("#move-file-modal");
	showFolderTree();
	modal.modal();
}

function deleteClicked() {
	let modal = $("#delete-file-modal");
	modal.modal();
}

function doDeleteFile() {
	let fileEncoded = encodeURIComponent("<%=fileName%>");
	let url = "library/book?file=" + fileEncoded;
	deleteJson(url, deleteCompleted);
}

function deleteCompleted() {
	let url = "folders.jsp?folder=" +  encodeURIComponent("<%=folderName%>");
	window.location.replace(url);
}

function doMoveCancel() {
	closeModal();
}

function doMoveFile() {
	let moveData = {};
	moveData.file = "<%=fileName%>";
	moveData.tofolder = selectedFolderForMove;
	putJson("library/book", moveData, putMoveCompleted);
}

function putMoveCompleted() {
	window.history.back();
}

function showFolderTree() {
	$('#foldertree').jstree({
		'core' : {
			'data' : {
				"url" : "library/folders?file=<%=fileEncoded%>",
				"dataType" : "json"
			}
		}
	});
	$('#foldertree').show();
}

function splitTitle() {
	let title = $("#txtBookTitle").val();
	let parts = title.split(" - ");
	if (parts.length == 2) {
		let newArtist = parts[0];
		$("#txtBookArtist").val(newArtist);
		let newTitle = parts[1];
		$("#txtBookTitle").val(newTitle);
	}
}

function tryFixTitlesOnPages() {
	for (page = 1; page <= <%=book.getNrOfPages()%>; page++) {
		let txtBoxTitleId = "txtPageTitle_" + page;
		let title = $("#" + txtBoxTitleId).val().trim();

		let txtArtistId = "txtPageArtist_" + page;
		let artist = $("#" + txtArtistId).val().trim();

		let txtBoxTitle2Id = "txtPageTitle2_" + page;
		let title2 = $("#" + txtBoxTitle2Id).val().trim();

		let txtArtist2Id = "txtPageArtist2_" + page;
		let artist2 = $("#" + txtArtist2Id).val().trim();

		let txtBoxDescriptionId = "txtPageText_" + page;
		let text = $("#" + txtBoxDescriptionId).val().trim();
		
		let newTitle = removeLeadingNumbers(title);
		let newArtist = artist;
		newTitle = newTitle.replace(".pdf", "");
		newTitle = newTitle.replace(".PDF", "");
		newTitle = newTitle.replace(".Pdf", "");
		newTitle = newTitle.replace(".jpg", "");
		newTitle = newTitle.replace(".Jpg", "");
		newTitle = newTitle.replace(".gif", "");
		newTitle = newTitle.replace(".GIF", "");
		newTitle = newTitle.replace(".Gif", "");
		if (newTitle.length > 1) {
			newTitle = newTitle.charAt(0).toUpperCase() + newTitle.slice(1).toLowerCase();
		}
		
		let split = newTitle.indexOf(" - ");
		if (split > -1) {
			newTitle = newTitle.substring(0, split);
			newArtist = title.substring(split + 3).trim();
			newArtist = removeLeadingNumbers(newArtist);
		}
		
		newArtist = capitalizeFirstLetterOfWords(artist);
		$("#" + txtBoxTitleId).val(newTitle);
		$("#" + txtArtistId).val(newArtist);
	}
}

function capitalizeFirstLetterOfWords(sentence) {
	const words = sentence.split(" ");
	let result = "";
	let first = true;
	for (let i = 0; i < words.length; i++) {
		let word = words[i];
		if (word.length > 0) {
			if (first) {
				first = false;
			} else {
				result += " ";
			}
			result += word[0].toUpperCase();
			if (word.length > 1) {
				result += word.substr(1).toLowerCase();
			}
		}
	}
	return result;
}


function removeLeadingNumbers(title) {
	const NUMBERS = "0123456789- ";
	let lettersFound = -1;
	let i = 0;
	for (i = 0; i < title.length && lettersFound == -1; i++) {
		let c = title.charAt(i);
		let index = NUMBERS.indexOf(c);
		if (index == -1) {
			lettersFound = i;		
		}
	}
	let result;
	if (lettersFound == -1) {
		result = "";
	} else {
		result = title.substring(lettersFound).trim();
	}
	return result;
}

function emptyTextFields() {
	for (page = 1; page <= <%=book.getNrOfPages()%>; page++) {
		let txtBoxTitleId = "txtPageTitle_" + page;
		$("#" + txtBoxTitleId).val("");
		let txtArtistId = "txtPageArtist_" + page;
		$("#" + txtArtistId).val("");
		let txtBoxTitle2Id = "txtPageTitle2_" + page;
	    $("#" + txtBoxTitle2Id).val("");
		let txtArtist2Id = "txtPageArtist2_" + page;
		$("#" + txtArtist2Id).val("");
		let txtBoxDescriptionId = "txtPageText_" + page;
		$("#" + txtBoxDescriptionId).val("");
	}
}

function initTextFieldPressUps() {
	for (let page = 1; page <= <%=book.getNrOfPages()%>; page++) {
		let textBoxTitleId = "#" + "txtPageTitle_" + page;
		let nextTextBoxTitleId = "#" + "txtPageTitle_" + (page + 1);
		$(textBoxTitleId).on("keyup", function(event) {
			if (event.keyCode === 13) {
				$(nextTextBoxTitleId).focus();
			}
		});
		let textBoxArtistId = "#" + "txtPageArtist_" + page;
		$(textBoxArtistId).on("keyup", function(event) {
			if (event.keyCode === 13) {
				$(nextTextBoxTitleId).focus();
			}
		});
	}
}




</script>

</head>

<body>
	<screenmusic:main-menu showSearch="false" />
	<div class="headerWithTitle">
		
			<i class="bi bi-arrow-left-short clickButton" onclick="javascript: window.history.back();"></i>
		
		<span class="pageTitle">Editing - <%=book.getLabel()%></span>
	</div>

	<div class="container">

		<input type="hidden" name="fileName" id="fileName" value="<%=fileName%>" />
		<div class="editImage">
			<img src="page?file=<%=fileEncoded%>&size=medium&page=1" />
		</div>

		<div class="editFields">
			<%=addInputText("txtBookTitle", "Title", book.getTitle(), false)%>
			<%=addInputText("txtBookArtist", "Artist", book.getArtist(), false, false, false, "\\/","splitTitle();return false;")%>
			<%=addInputText("txtBookSeries", "Series", book.getSeries(), false)%>
			<label for="categories">Categories</label>
			<div id="categories"></div>
			<div class='editOkCancel'>
				<button type="submit" onclick="doPostUpdate();">OK</button>
				<button type="button" onclick='window.history.back();'>Cancel</button>
			</div>
		</div>

		<div class="editFields">
			<%=addInputText("txtBookFileName", "File", bookFileName, false, false, true)%>
			<%=addInputText("txtBookFolderName", "Folder", folderName, false, false, true)%>
			<button class="bookManagementButton" type="button" onclick="moveClicked();return false;">Move</button>
			<button class="bookManagementButton" type="button" onclick="deleteClicked();return false;">Delete</button>
		</div>

		<br style="clear: both;" />
		<h1>Pages</h1>
		<button class="bookManagementButton" type="button" onclick="emptyTextFields();return false;">Empty pages fields</button>
		<button class="bookManagementButton" type="button" onclick="tryFixTitlesOnPages();return false;">Try fix page titles</button>
		<br style="clear: both;" />

		<%
		int pageNumber = 1;
		for (BookPage bookPage : book.getPages()) {
			String id = "_" + pageNumber;
			String textFieldTitleId = "txtPageTitle" + id;
			%>
			<br style="clear: both;" />
			<div class="editImage">
				<img src="page?file=<%=fileEncoded%>&size=medium&page=<%=pageNumber%>" />
			</div>
			<div class="editFields">
				<%=addInputText(textFieldTitleId, "Title", bookPage.getTitle(), false)%>
				<%=addInputText("txtPageArtist" + id, "Artist", bookPage.getArtist(), false)%>
				<%=addInputText("txtPageTitle2" + id, "Second title", bookPage.getTitle2(), false)%>
				<%=addInputText("txtPageArtist2" + id, "Second artist", bookPage.getArtist2(), false)%>
				<%=addInputText("txtPageText" + id, "Notes", bookPage.getText(), false, true)%>
			</div>
			<%
			pageTitleIds.add(textFieldTitleId);
			pageNumber++;
		}
		%>
		
		<br style="clear: both;" />
		<div class='editOkCancel'>
			<button type="submit" onclick="doPostUpdate();">OK</button>
			<button type="button" onclick='window.history.back();'>Cancel</button>
		</div>

	</div>

	<div id="move-file-modal" class="modal">
		<p>
			Book file:
			<%=fileName%></p>
		<p>
			Current folder:
			<%=folderName%></p>
		<p>Move to
		<p>
		<div id="foldertree"></div>
		<br style="clear: both;" />
		<div class='editOkCancel'>
			<button type="submit" onclick="doMoveFile();">OK</button>
			<button type="button" onclick='doMoveCancel();return false;'>Cancel</button>
		</div>
	</div>

	<div id="delete-file-modal" class="modal">
		<p>
			Book file:
			<%=fileName%></p>
		<p>
			Current folder:
			<%=folderName%></p>
		<p>Delete this file?
		<p>
		<div id="foldertree"></div>
		<br style="clear: both;" />
		<div class='editOkCancel'>
			<button type="button" onclick="doDeleteFile();">Yes</button>
			<button type="submit" onclick='doMoveCancel();return false;'>No</button>
			<button type="button" onclick='doMoveCancel();return false;'>Cancel</button>
		</div>
	</div>

</body>

<script>

var selectedFolderForMove;

function folderTreeClicked(id) {
	selectedFolderForMove = id;
}

$('#foldertree').on("changed.jstree", function(e, data) {
	folderTreeClicked(data.selected);
});

$('#txtBookSeries').autocomplete({
	serviceUrl : 'autocomplete-series',
	minChars : 1
});


function documentReady() {
	initCategories();
	initTextFieldPressUps();
}

$(document).ready(documentReady());

</script>
</html>