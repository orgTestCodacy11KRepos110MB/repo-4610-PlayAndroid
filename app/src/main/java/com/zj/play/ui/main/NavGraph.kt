/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zj.play.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.paging.ExperimentalPagingApi
import com.google.gson.Gson
import com.zj.play.logic.model.ArticleModel
import com.zj.play.logic.utils.getHtmlText
import com.zj.play.ui.main.PlayDestinations.ARTICLE_ROUTE_URL
import com.zj.play.ui.page.article.ArticlePage
import com.zj.play.ui.page.login.LoginPage
import java.net.URLEncoder

/**
 * Destinations used in the ([NewMainActivity]).
 */
object PlayDestinations {
    const val HOME_PAGE_ROUTE = "home_page_route"
    const val ARTICLE_ROUTE = "article_route"
    const val ARTICLE_ROUTE_URL = "article_route_url"
    const val LOGIN_ROUTE = "login_route"
}


@ExperimentalPagingApi
@Composable
fun NavGraph(
    startDestination: String = PlayDestinations.HOME_PAGE_ROUTE
) {
    val navController = rememberNavController()

    val actions = remember(navController) { PlayActions(navController) }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(PlayDestinations.HOME_PAGE_ROUTE) {
            MainPage(actions)
        }
        composable(PlayDestinations.LOGIN_ROUTE) {
            LoginPage(actions)
        }
        composable(
            "${PlayDestinations.ARTICLE_ROUTE}/{$ARTICLE_ROUTE_URL}",
            arguments = listOf(navArgument(ARTICLE_ROUTE_URL) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val arguments = requireNotNull(backStackEntry.arguments)
            val parcelable = arguments.getString(ARTICLE_ROUTE_URL)
            val fromJson = Gson().fromJson(parcelable, ArticleModel::class.java)
            ArticlePage(
                article = fromJson,
                onBack = actions.upPress
            )
        }
    }
}

/**
 * 对应用程序中的导航操作进行建模。
 */
class PlayActions(navController: NavHostController) {

    val enterArticle: (ArticleModel) -> Unit = { article ->
        article.desc = ""
        article.title = getHtmlText(article.title)
        val gson = Gson().toJson(article).trim()
        val result = URLEncoder.encode(gson, "utf-8")
        navController.navigate("${PlayDestinations.ARTICLE_ROUTE}/$result")
    }
    val toLogin: () -> Unit = {
        navController.navigate(PlayDestinations.LOGIN_ROUTE)
    }
    val upPress: () -> Unit = {
        navController.navigateUp()
    }

}