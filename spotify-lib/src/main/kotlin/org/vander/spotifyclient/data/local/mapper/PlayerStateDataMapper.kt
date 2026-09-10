package org.vander.spotifyclient.data.local.mapper

import org.vander.core.domain.state.DomainPlayerState
import org.vander.core.domain.state.PlayerStateData

fun PlayerStateData.toAppPlayerState(isSaved: Boolean): DomainPlayerState = DomainPlayerState(this, isSaved)
