package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationDataById;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.*;

import java.util.Map;

/**
 * User's application detail
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfApplicationDetailTabItem implements TabItem, TabItemWithUrl{

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	final ScrollPanel sp = new ScrollPanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Loading application");

	private Application application;
	private int applicationId = 0;
	private User user;


	/**
	 * Creates a tab instance
	 * @param application
	 */
	public SelfApplicationDetailTabItem(Application application){
		this.application = application;
		this.applicationId = application.getId();
		this.user = application.getUser();
	}

	/**
	 * Creates a tab instance
	 * @param applicationId
	 */
	public SelfApplicationDetailTabItem(int applicationId) {
		this.applicationId = applicationId;
		new GetEntityById(PerunEntity.APPLICATION, applicationId, new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				application = jso.cast();
				user = application.getUser();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(application == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+ ": Application detail");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		String header = "<h2>";
		if (application.getType().equalsIgnoreCase("INITIAL") || application.getType().equalsIgnoreCase("EMBEDDED")) {
			header += "Initial application for ";
		} else {
			header += "Extension application for ";
		}
		if (application.getGroup() == null) {
			header += "VO "+application.getVo().getName();
		} else {
			header += "group "+application.getGroup().getShortName()+" in VO "+application.getVo().getName();
		}

		String submitted = "</h2><p>Submitted on <strong>" + ((application.getCreatedAt().contains(".")) ? application.getCreatedAt().substring(0, application.getCreatedAt().indexOf(".")) : application.getCreatedAt());
		submitted += "</strong> is in state <strong>"+application.getState().toUpperCase()+"</strong>";

		vp.add(new HTML(header+submitted));

		vp.add(new HTML("<hr size=\"1\" style=\"color: #ccc;\" />"));

		sp.setSize("100%", "100%");
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		if (application.getType().equalsIgnoreCase("EMBEDDED")) {
			final FlexTable ft = new FlexTable();
			ft.setCellSpacing(5);
			String userName = application.getUser().getFullNameWithTitles();
			if (userName != null) {
				ft.setHTML(0, 0, "<strong>Name:</strong> " + SafeHtmlUtils.fromString(userName).asString());
			}
			new GetEntityById(PerunEntity.RICH_USER_WITH_ATTRS, application.getUser().getId(), new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					User user = jso.cast();
					Attribute preferredMail = user.getAttribute("urn:perun:user:attribute-def:def:preferredMail");
					if (preferredMail != null) {
						ft.setHTML(1, 0, "<strong>Preferred mail:</strong> " + SafeHtmlUtils.fromString(preferredMail.getValue()).asString());
					}
				}
			}).retrieveData();
			sp.setWidget(ft);
		} else {
			GetApplicationDataById data = new GetApplicationDataById(applicationId);
			data.setShowAdminItems(false);
			data.retrieveData();
			sp.setWidget(data.getContents());
		}

		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				// run resize only for opened tab/overlay + shared commands
				resizeTable();
			}
		});

		this.contentWidget.setWidget(vp);
		resizeTable();
		return getWidget();

	}

	/**
	 * Resize table to the max width possible based on tab content width
	 */
	private void resizeTable() {

		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				if (sp.getWidget() != null && contentWidget != null) {
					((SimplePanel)sp.getWidget()).setWidth((Math.max(contentWidget.getOffsetWidth()-5, 0)+"px"));
				}
			}
		});

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.applicationFromStorageIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1223;
		int result = 43;
		result = prime * result * applicationId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (this.applicationId != ((SelfApplicationDetailTabItem)obj).applicationId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
		if (user != null) {
			session.setActiveUser(user);
		}
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "My applications", UsersTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"appls?id="+application.getUser().getId(), "Application detail", getUrlWithParameters());
		resizeTable();
	}

	public boolean isAuthorized() {

		// GETTING APPLICATION WITHOUT USER SET IS NOT ALLOWED IN PERUN GUI,
		// SINCE USER MUST BE LOGGED-IN !!
		if (user != null) {
			if (session.isSelf(user.getId())) {
				// is authorized for user from application
				return true;
			}
		}
		return false;

	}

	public final static String URL = "appl-detail";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + applicationId;
	}

	static public SelfApplicationDetailTabItem load(Map<String, String> parameters) {

		if (parameters.containsKey("id")) {
			int appid = Integer.parseInt(parameters.get("id"));
			if (appid != 0) {
				return new SelfApplicationDetailTabItem(appid);
			}
		}
		return null;
	}

}
