<!DOCTYPE html>
<%@ taglib prefix="sheetmusic" uri="WEB-INF/tags.tld"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>

<head>
<jsp:include page="head.jsp"></jsp:include>
<script src="app/artist.js"></script>
</head>

<body>
	<sheetmusic:main-menu selected="Artists" showSearch="true" />
	<div id="sheetmusic-main-body">
		<div class="headerWithTitle">
			<icons:icon name="chevron-left" cssClass="clickButton" size="24" onclick="javascript: window.history.back();" /> 
			<span class="pageTitle" id="artist">Artist</span>
			<span style="margin-left: 16px"> 
				<icons:icon id="favorites-yes" name="star-fill" cssClass="clickButton" onclick="favoritesBookClicked('yes');"/>
				<icons:icon id="favorites-no" name="star" cssClass="clickButton" onclick="favoritesBookClicked('no');"/>
			</span>
		</div>
		<div id="books" class="category-items"></div><br>
		<div id="songs" class="category-items"></div>
	</div>
	<div id="sheetmusic-search-results"></div>
</body>

</html>