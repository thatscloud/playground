package org.thatscloud.playground.route.rest;

import static spark.Spark.get;

import java.util.Map;

import org.thatscloud.playground.players.PlayersContainer;
import org.thatscloud.playground.route.management.RegistrableRoute;

public class RankingsRestRoute extends RegistrableRoute {

	boolean test = true;

	public RankingsRestRoute() {
	}

	@Override
	public void register() {
		if (test) {
			get("/rest/rankings", "application/json", (request, response) -> {
				Map<String, Object> page = getNewPageModel(request);
				page.put("testData", "testtesttest}");
				return page;
			}, getJsonTransformer());
		} else {
			get("/rankings", "application/json", (request, response) -> {
				Map<String, Object> page = getNewPageModel(request);
				page.put("players", PlayersContainer.theDisplayPlayers);
				return page;
			}, getJsonTransformer());
		}
	}

}
