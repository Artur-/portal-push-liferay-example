package com.example.portalpush;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("mytheme")
@SuppressWarnings("serial")
@Push(transport = Transport.WEBSOCKET_XHR)
public class MyPortletUI extends UI {

    private static Log log = LogFactoryUtil.getLog(MyPortletUI.class);
    private ScheduledExecutorService service = Executors
            .newScheduledThreadPool(1);

    @Override
    protected void init(VaadinRequest request) {
        final String portletContextName = getPortletContextName(request);
        final Integer numOfRegisteredUsers = getPortalCountOfRegisteredUsers();
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        final Button button = new Button("Click Me");
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                layout.addComponent(new Label(
                        "Hello, World!<br>This is portlet "
                                + portletContextName
                                + ".<br>This portal has "
                                + numOfRegisteredUsers
                                + " registered users (according to the data returned by Liferay API call).",
                                ContentMode.HTML));

            }
        });
        layout.addComponent(button);
        service.scheduleWithFixedDelay(new Runnable() { // Run every second, as
                                                        // defined below
                    @Override
                    public void run() {
                        access(new Runnable() {// Synchronize with Vaadin
                            @Override
                            public void run() {
                                layout.addComponent(new Label(
                                        "Server time is now: " + new Date()));
                            }
                        });
                    }
                }, 1, 1, TimeUnit.SECONDS);

    }

    private String getPortletContextName(VaadinRequest request) {
        WrappedPortletSession wrappedPortletSession = (WrappedPortletSession) request
                .getWrappedSession();
        PortletSession portletSession = wrappedPortletSession
                .getPortletSession();

        final PortletContext context = portletSession.getPortletContext();
        final String portletContextName = context.getPortletContextName();
        return portletContextName;
    }

    private Integer getPortalCountOfRegisteredUsers() {
        Integer result = null;

        try {
            result = UserLocalServiceUtil.getUsersCount();
        } catch (SystemException e) {
            log.error(e);
        }

        return result;
    }
}
