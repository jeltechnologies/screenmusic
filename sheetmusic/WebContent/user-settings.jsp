<!DOCTYPE html>
<html>
<%@ taglib prefix="sheetmusic" uri="WEB-INF/tags.tld"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="com.jeltechnologies.screenmusic.tags.BaseTag"%>
<head>
<jsp:include page="head.jsp"></jsp:include>

<style>

.background-tasks-container {
	background-color: var(--grey);
	padding: 8px;
}

.background-tasks-table {
	border: 1px solid rgba(0, 0, 0, 0.8);
	padding: 8px;
	margin: 8px;
	width: 95%;
}

.background-tasks-column, .background-tasks-header {
	text-align: left;
	padding: 8px;
}

#tasktable tr {
	background-color: white;
}

#tasktable button {
	width: auto;
}

</style>

</head>

<body>
	<sheetmusic:main-menu />
	<div class="background-tasks-container">
		<div class="category-title">Musescore exports</div>
		<div id="background-tasks-musescore"></div>
	</div>
</body>

<script>
	function backgroundTasksDocumentReady() {
		refreshTable();
		setInterval(refreshTable, 2000);
	}
	
	function refreshTable() {
		getJson("tasks-musicxml", updatetaskMuseScore);
	}
	

	function updatetaskMuseScore(data) {
		let taskLines = enforceArrayFromElements(data);
		let html = "";
		html = html + "<table class='background-tasks-table' id='tasktable'><tr style='background: var(--grey)'>";
		html = html + "<tr>";
		html = html + taskHeader("Date");
		html = html + taskHeader("Time");
		html = html + taskHeader("Book");
		html = html + taskHeader("Pages");
		html = html + taskHeader("Status");
		html = html + taskHeader("Action");
		html = html + "</tr>";
		if (taskLines != undefined && taskLines.length > 0) {
			for (let i = 0; i < taskLines.length; i++) {
				html = html + "<tr>";
				let job = taskLines[i];

				html = html + taskColumn(getTimeAgo(job.startTime));
				html = html + taskColumn(formatTime(job.startTime));
				html = html + taskColumn(job.label);
				html = html + taskColumn(job.from + " - " + job.to);

				let status = job.status;
				if (status === "In progress") {
					status = status + " - " + job.percentageCompleted + "%";
				}
				html = html + taskColumn(status);
				
				let action = "";
				let finished;
				let download = false;
				
				let disableDownload = "disabled";
				let disableCancel = "disabled";
				
				if (job.status === "Ready for download" || job.status === "Error") {
					if (job.status === "Ready for download") {
						disableDownload = "";
					}
				}
				
				action = action + "<button " + disableDownload + " onclick=\"downloadExportResultClicked('" + job.id + "');\">Download</button>";
				action = action + "<button " + " onclick=\"deleteJobClicked('" + job.id + "');\">Delete</button>";

				html = html + taskColumn(action);
				html = html + "</tr>";
			}
		}
		html = html + "</table>";

		$('#background-tasks-musescore').html(html);
	}

	function downloadExportResultClicked(id) {
		let location = "download-job?id=" + id;
		window.location = location;
		closeModal();
	}
	
	function deleteJobClicked(id) {
		let location = "tasks-musescore?id=" + id;
		deleteJson(location, deleteCompleted);
	}
	
	function deleteCompleted(data) {
	}

	function taskColumn(text) {
		let html = '<td class="background-tasks-column">' + text + '</td>';
		return html;
	}

	function taskHeader(text) {
		let html = '<th class="background-tasks-header">' + text+ '</th>';
		return html;
	}

	$(document).ready(backgroundTasksDocumentReady());
</script>

</html>
