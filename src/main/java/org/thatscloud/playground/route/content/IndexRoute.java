package org.thatscloud.playground.route.content;

import static spark.Spark.get;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.thatscloud.playground.route.management.RegistrableRoute;

import spark.Request;
import spark.Response;

public class IndexRoute extends RegistrableRoute {

	public IndexRoute() {
	}

	@Override
	public Object handle(final Request request, final Response response) throws Exception {
		try (final InputStream in = getClass().getClassLoader().getResourceAsStream("org/thatscloud/playground/ng/pages/index.html");
				final OutputStream out = response.raw().getOutputStream()) {
			IOUtils.copy(in, out);
		} catch (Exception e) {
			response.redirect(getFullUrl(request, "/404"));
		}
		return response.raw();
	}

	@Override
	public void register() {
		get("/", this);
		get("/index", this);
	}

}
