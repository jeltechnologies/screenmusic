<!DOCTYPE html>
<%@page import="com.jeltechnologies.screenmusic.User"%>
<%@page import="com.jeltechnologies.screenmusic.servlet.ScreenMusicContext"%>
<%@page import="com.jeltechnologies.screenmusic.servlet.BaseServlet"%>
<html>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" errorPage="error.jsp"%>
<%@page import="com.jeltechnologies.utils.StringUtils"%>
<%@page import="com.jeltechnologies.screenmusic.library.BookPage"%>
<%@page import="com.jeltechnologies.screenmusic.library.Book"%>
<%@page import="com.jeltechnologies.screenmusic.library.Library"%>
<%@ taglib prefix="sheetmusic" uri="WEB-INF/tags.tld"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>

<%
ScreenMusicContext sheetMusicContext = new ScreenMusicContext(application);
User user = BaseServlet.getUser(request);
String id = (String) request.getParameter("id");
Book book = new Library(user, sheetMusicContext).getBook(id);

if (book == null) {
	response.setStatus(404);
	response.sendError(404, "Not Found");
}
request.setAttribute("book", book);

String headerLabel;
String artist = book.getArtist();
String title = book.getTitle();
artist = StringUtils.encodeHtml(artist);
title = StringUtils.encodeHtml(title);
if (artist == null || artist.isBlank()) {
	headerLabel = StringUtils.encodeHtml(book.getLabel());
} else {
	headerLabel = "<a href='artist.jsp?artist=" + StringUtils.encodeURL(book.getArtist()) + "'>" + artist + "</a> - " + title;
}
%>
<head>
<jsp:include page="head.jsp"></jsp:include>
<script src="app/book.js"></script>
</head>

<body>
	<sheetmusic:main-menu showSearch="false" />

	<div id="sheetmusic-main-body">

		<div class="headerWithTitle">
			<icons:icon name="arrow-left" cssClass="clickButton" onclick="javascript: window.history.back();" />
			<span class="pageTitle"><%=headerLabel%></span> 
			
			<span style="margin-left: 16px"> 
				<icons:icon id="favorites-yes" name="star-fill" cssClass="clickButton" onclick="favoritesBookClicked('yes');"/>
				<icons:icon id="favorites-no" name="star" cssClass="clickButton" onclick="favoritesBookClicked('no');"/>
				<icons:icon name="arrow-clockwise" cssClass="clickButton" onclick="userClickedRefresh();" /> 
				<icons:icon name="pencil-square" cssClass="clickButton" onclick="editClicked();" /> 
				<icons:icon name="download" cssClass="clickButton" onclick="downloadClicked();" />
			</span>
		</div>

		<div id="bookthumb">
			<sheetmusic:bookpages />
		</div>

		<div class="modal" id="refreshModal" style="display: none;">
			<h3>
				<span id="refhreshModal">Refresh</span>
			</h3>

			<p>Refresh all pages from original PDF file?</p>
			<p>This may take several minutes to complete.</p>
			<div class="favoritemodalbuttons">
				<button type="button" id="refreshModalOK" onclick="refreshModalYes();">Yes</button>
				<button type="button" id="refreshModalDelete" onclick="refreshModalNo();">No</button>
				<button type="button" onclick="refreshModalNo();">Cancel</button>
			</div>
		</div>

	</div>

	<script>
		$(document).ready(documentReadyForBook());
	</script>
</body>
</html>