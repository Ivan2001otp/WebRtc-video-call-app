package com.ivan.webrtcvideochat.Helper

import com.ivan.webrtcvideochat.Model.User

interface OnCallClick {
   fun onItemCallClickListener(user: User)
}