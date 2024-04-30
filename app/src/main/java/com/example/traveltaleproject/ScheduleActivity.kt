import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.R

class ScheduleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SchduleItemAdapter
    private lateinit var cardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        recyclerView = findViewById(R.id.schedule_item)
        adapter = SchduleItemAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        cardView = findViewById(R.id.schedule_item_add)
        cardView.setOnClickListener {
            addItem()
        }
    }

    private fun addItem() {
        val newItem = "New Item"
        adapter.addItem(newItem)
    }
}