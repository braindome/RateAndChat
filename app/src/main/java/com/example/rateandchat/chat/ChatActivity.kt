package com.example.rateandchat.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rateandchat.BasicActivity
import com.example.rateandchat.R
import com.example.rateandchat.dataclass.Message
import com.example.rateandchat.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

// 1 on 1 chatroom
class ChatActivity : BasicActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox : EditText
    private lateinit var sendButton : ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var db : FirebaseFirestore
    private lateinit var messagesRef : CollectionReference
    private lateinit var usersRef : CollectionReference

    var receiverRoom : String? = null
    var senderRoom : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)



        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        db = Firebase.firestore
        messagesRef = db.collection("Chats")
        usersRef = db.collection("Users")

        /* Creates different rooms, depending on who
        * receives and who sends the message */

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name

        chatRecyclerView = findViewById(R.id.chatRecycleView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)

        /* Initialization of message list, message adapter,
        * inflates the layout */

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        /* Reads data from Firebase, orders it by time,
        converts it to Message object and adds it to the RecyclerView */

        // Koden körs i denna ordning: 1, 3, 2 --> 2 --> 2 --> 2 ...
        // Även snapshot listener är en asynkron operation. Till skillnaden med get, som körs bara en gång,
        // här kommer data hämtas från db varje gång det sker en ändring i själva db.
        // Det vill säga, varje gång man lägger till ett meddelande till databasen kommer denna förändras, och snapshot uppdateras.
        
        // 1
        messagesRef.document(senderRoom!!).collection("Messages")
            .orderBy("timestamp")
            .addSnapshotListener() { snapshot, e ->
                messageList.clear()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val item = document.toObject<Message>()
                        messageList.add(item!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }
                // 2
            }
        // 3


        /* Firebase query for sender's name */

        // Operationerna körs i denna ordning: 1, 2, 4, 5, --> 3
        // Getten körs asynkront: man båbörjar hämtningen av datam, och koden utanför .get() körs samtidigt som appen hämtar resultaten från Firestore.
        // Så programmet kommer inte stanna, och istället fortsätter.
        // Sändarnamnet behöver hämtas varje gång denne skickar ett meddelande, då objektet skapas och laddas upp som dokument på Firestore.

        // 1
        sendButton.setOnClickListener {

            // 2
            val message = messageBox.text.toString()
            usersRef.whereEqualTo("uid", senderUid)
                .get()
                .addOnSuccessListener { documents ->
                    // 3
                    for (document in documents) {
                        Log.d("nameQuery", "${document.id} => ${document.data}")
                        val senderName = document.toObject<User>().name.toString()
                        val senderProfilePic = document.toObject<User>().profilePic.toString()
                        val messageObject = Message(message, senderUid, senderName, senderProfilePic)

                        addMsgToDatabase(messageObject, senderName, senderProfilePic)

                    }
                }
                .addOnFailureListener {exception ->
                    Log.w("nameQuery", "Error getting documents: ", exception)
                }
            // 4

        }
        // 5
    }

    /* Adds messge to Firebase */
    private fun addMsgToDatabase(messageObject : Message, senderName : String, senderProfilePic : String) {
        messagesRef.document(senderRoom!!).collection("Messages")
            .add(messageObject)
            .addOnSuccessListener { documentReference ->
                messagesRef.document(receiverRoom!!).collection("Messages")
                    .add(messageObject)
                Log.d("msg", "DocumentSnapshot written with ID: ${documentReference.id}")
                Log.d("msg", "Sender name is: $senderName")
            }
            .addOnFailureListener { e ->
                Log.d("msg", "Error adding document", e)
            }
        messageBox.setText("")
    }
}