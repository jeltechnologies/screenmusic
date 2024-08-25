package com.jeltechnologies.screenmusic.tags;

import java.util.ArrayList;
import java.util.List;

import com.jeltechnologies.icons.IconTag;
import com.jeltechnologies.utils.StringUtils;
import com.jeltechnologies.utils.datatypes.NamedValue;

public class MainMenuTag extends BaseTag {
    private String selected = null;

    private boolean showSearch = false;

    private static final List<NamedValue> MENU_ITEMS;
    
    private static final String CLEAR_SEARCH_ICON;
    
    private static final String LOGO_ICON = new IconTag("music-note-beamed", 24).toString();

    static {
	MENU_ITEMS = new ArrayList<NamedValue>();
	MENU_ITEMS.add(new NamedValue("Categories", "categories.jsp"));
	MENU_ITEMS.add(new NamedValue("Series", "series.jsp"));
	MENU_ITEMS.add(new NamedValue("Favorites", "favorites.jsp"));
	MENU_ITEMS.add(new NamedValue("Books", "books.jsp"));
	MENU_ITEMS.add(new NamedValue("Artists", "artists.jsp"));
	MENU_ITEMS.add(new NamedValue("Folders", "folders.jsp"));
	MENU_ITEMS.add(new NamedValue("History", "history.jsp"));
	MENU_ITEMS.add(new NamedValue("Stats", "statistics.jsp"));
	IconTag icon = new IconTag("x");
	icon.setOnclick("clearSearchText();");
	CLEAR_SEARCH_ICON = icon.toString();
    }

    public void setSelected(String selected) {
	this.selected = selected;
    }

    public void setShowSearch(boolean showsearch) {
	this.showSearch = showsearch;
    }

    @Override
    public void processTag() throws Exception {
	addLine("<div id=\"main-menu\">");
	add("<div class=\"sitelogo\" onclick=\"mainMenuSiteLogoClicked()\">");
	add(LOGO_ICON);
	addLine("</div>");
	
	if (showSearch) {
	    add("<div><input type=\"text\" id=\"main-menu-search-input\" placeholder=\"Search..\"/>");
	    add(CLEAR_SEARCH_ICON);
	    addLine("</div>");
	}

	addLine("<div id=\"sheetmusic-menu-items\">"); 
	
	for (NamedValue item : MENU_ITEMS) {
	    StringBuilder b = new StringBuilder();
	    boolean itemIsSelected = item.getName().equalsIgnoreCase(selected);
	    b.append("<div class=\"");
	    if (itemIsSelected) {
		b.append("view-selected\"><a href=\"").append(item.getValue()).append("\">");
	    } else {
		b.append("view\"><a href=\"").append(item.getValue()).append("\">");
	    }
	    b.append(item.getName());
	    b.append("</a>");
	    b.append("</div>");
	    addLine(b.toString());
	}
	
	addLine("<div>");
	add(new IconTag("person").toString());
	
	String userName = getUser().name();
	StringBuilder link = new StringBuilder();

	if (userName == null || userName.equals("")) {
	    link.append("<a href=\"/login.jsp\">Login</a>");
	} else {
	    link.append("<a href=\"user-settings.jsp\">")
	    	.append(StringUtils.encodeHtml(userName))
	    	.append("</a>");
	}
	addLine(link.toString());
	
	addLine("</div>");
	addLine("</div>");
	addLine("</div>");
    }

   
    @SuppressWarnings("unused")
    private void addUserMenus() throws Exception {
	addLine("<div class='dropdown-content'>");
	addLine("  " + getUserMenu("Settings", "settingsClicked();"));
	addLine("  " + getUserMenu("Background tasks", "window.open('background-tasks.jsp', '_self');"));
	addLine("  " + getUserMenu("Log out", "logoutClicked();"));
	addLine("</div>");
    }

    private String getUserMenu(String title, String javaScriptFunction) {
	StringBuilder b = new StringBuilder();
	b.append("<a href=\"javascript:").append(javaScriptFunction).append("\">");
	b.append(title).append("</a>");
	return b.toString();
    }

}
