package nl.nn.ibistesttool;

import nl.nn.adapterframework.configuration.IbisContext;
import nl.nn.adapterframework.lifecycle.DynamicRegistration;
import nl.nn.adapterframework.lifecycle.IbisApplicationServlet;
import nl.nn.adapterframework.lifecycle.IbisInitializer;
import nl.nn.adapterframework.lifecycle.ServletManager;
import nl.nn.adapterframework.util.LogUtil;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;

@IbisInitializer
public class LadybugServlet extends CXFServlet implements DynamicRegistration.ServletWithParameters {
	Logger log = LogUtil.getLogger(this);
	// These are the beans that will be available directly under servlet context for endpoints.
	private final String[] beans = new String[]{"testTool", "reportXmlTransformer", "runStorage", "logStorage"};

	@Override
	public void init(ServletConfig sc) throws ServletException {
		System.err.println("INITING LADYBUG SERVLET FROM IAF!!!!");
		super.init(sc);

		// Make sure testtool related beans are registered as attributes for servlet context.
		// So they can be accessed from ibis-ladybug.
		ServletContext servletContext = sc.getServletContext();
		IbisContext context = IbisApplicationServlet.getIbisContext(servletContext);
		for (String bean : beans) {
			try {
				log.debug("Ladybug Servlet registering bean [" + bean + "]");
				servletContext.setAttribute(bean, context.getBean(bean));
			} catch (NoSuchBeanDefinitionException e) {
				log.error("No bean named [" + bean + "] available inside the Ibis Context.", e);
			}
		}
	}

	@Autowired
	public void setServletManager(ServletManager servletManager) {
		servletManager.register(this);
	}

	@Override
	public HttpServlet getServlet() {
		return this;
	}

	@Override
	public String getUrlMapping() {
		return "ladybug/*";
	}

	@Override
	public String[] getRoles() {
		return null;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("config-location", "LadybugWebContext.xml");
		parameters.put("bus", "ladybug-api-bus");
		return parameters;
	}

	@Override
	public String getName() {
		return "Ladybug Servlet";
	}

	@Override
	public int loadOnStartUp() {
		return 0;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// This event listens to all Spring refresh events.
		// When adding new Spring contexts (with this as a parent) refresh events originating from other contexts will also trigger this method.
		// Since we never want to reinitialize this servlet, we can ignore the 'refresh' event completely!
	}
}
