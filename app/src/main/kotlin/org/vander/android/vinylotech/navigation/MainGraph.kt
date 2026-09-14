package org.vander.android.vinylotech.navigation

import kotlinx.serialization.Serializable

/**
 * The post-login area: the tabs and everything pushed from them.
 *
 * Beyond grouping, it earns its place twice. It gives `popUpTo` a single target that pops
 * the whole post-login stack at once, and it provides the `NavBackStackEntry` the
 * MiniPlayer's ViewModel is scoped to — one that outlives a tab change, unlike the entry
 * of any single destination.
 *
 * `object` and not `class`: a route is a value, and a graph taking no argument is a single
 * one. `@Serializable` is not decoration either — `navigation`, `navigate`, `popUpTo`,
 * `hasRoute` and `getBackStackEntry` all derive the destination id from
 * `serializer<T>().generateHashCode()`, and fail at runtime without it.
 */
@Serializable
object MainGraph
