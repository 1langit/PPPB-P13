package com.example.pppb_p13

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import com.example.pppb_p13.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val firestre = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestre.collection("budgets")
    private lateinit var  binding: ActivityMainBinding
    private var updateId = ""
    private val budgetListLiveData: MutableLiveData<List<Budget>> by lazy {
        MutableLiveData<List<Budget>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnAdd.setOnClickListener {
                val nominal = edtNominal.text.toString()
                val desc = edtDesc.text.toString()
                val date = edtDate.text.toString()
                val newBudget = Budget(nominal = nominal, description = desc, date = date)
                addBudget(newBudget)
            }

            btnUpdate.setOnClickListener {
                val nominal = edtNominal.text.toString()
                val desc = edtDesc.text.toString()
                val date = edtDate.text.toString()
                val updateBudget = Budget(nominal = nominal, description = desc, date = date)
                updateBudget(updateBudget)
                updateId = ""
                setEmptyField()
            }

            listView.setOnItemClickListener {
                adapterView, view, i, l ->
                val item = adapterView.adapter.getItem(i) as Budget
                updateId = item.id
                edtDesc.setText(item.description)
                edtDate.setText(item.date)
                edtNominal.setText(item.nominal)
            }

            listView.setOnItemLongClickListener {
                adapterView, view, i, l ->
                val item = adapterView.adapter.getItem(i) as Budget
                deleteBudget(item)
                true
            }
        }

        observeBudgets()
        getAllBudgets()
    }

    private fun getAllBudgets() {
        observeBudgetChanges();
    }

    private fun observeBudgetChanges() {
        budgetCollectionRef.addSnapshotListener {
            snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening to budget changes")
            }
            val budgets = snapshots?.toObjects(Budget::class.java)
            if (budgets != null) {
                budgetListLiveData.postValue(budgets)
            }
        }
    }

    private fun observeBudgets() {
        budgetListLiveData.observe(this) {
            budgets ->
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                budgets.toMutableList()
            )
            binding.listView.adapter = adapter
        }
    }

    private fun addBudget(budget: Budget) {
        budgetCollectionRef.add(budget).addOnSuccessListener {
            docummentReference ->
            val createBudgetId = docummentReference.id
            budget.id = createBudgetId
            docummentReference.set(budget).addOnFailureListener {
                Log.d("MainActivity", "Error updating budget id : ", it)
            }
        }.addOnFailureListener {
            Log.d("MainActivity", "Error adding budget id : ", it)
        }
    }

    private fun updateBudget(budget: Budget) {
        budget.id = updateId
        budgetCollectionRef.document(updateId).set(budget).addOnFailureListener {
            Log.d("MainActivity", "error updating budget", it)
        }
    }

    private fun deleteBudget(budget: Budget) {
        if (budget.id.isEmpty()) {
            Log.d("MainActivity", "error delete item!")
            return
        }
        budgetCollectionRef.document(budget.id).delete().addOnFailureListener {
            Log.d("MainActivityy", "Error deleting budget", it)
        }
    }

    private fun setEmptyField() {
        with(binding) {
            edtNominal.setText("")
            edtDate.setText("")
            edtDesc.setText("")
        }
    }
}