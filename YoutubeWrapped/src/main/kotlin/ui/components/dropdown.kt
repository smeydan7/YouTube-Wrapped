package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import userApiKit.FriendUser

@Composable
fun FriendsDropdown(
    matches: List<FriendUser>,
    onFriendSelected: (FriendUser) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(350.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE5E5E5))
            .padding(8.dp)
    ) {
        if (matches.isEmpty()) {
            // If no matches found, display a message
            Text("No matches found", fontWeight = FontWeight.Bold)
        } else {
            Text("Search Results:", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            matches.forEach { friend ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onFriendSelected(friend)
                            onDismiss()
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = "${friend.first_name} ${friend.last_name} (@${friend.username})",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}