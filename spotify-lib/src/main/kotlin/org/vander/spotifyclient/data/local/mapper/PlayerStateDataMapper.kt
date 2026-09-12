package org.vander.spotifyclient.data.local.mapper

import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.PlayerStateData

/** Pairs a raw player snapshot with the saved-track flag fetched from the Web API. */
fun PlayerStateData.toAppPlayerState(isSaved: Boolean): DomainPlayerState = DomainPlayerState(this, isSaved)
