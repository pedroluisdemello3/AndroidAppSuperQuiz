package com.jailton.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jailton.androidapptemplate.R
import com.jailton.androidapptemplate.baseclasses.Item
import com.jailton.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private lateinit var enderecoEditText: EditText
    //TODO("Declare aqui as outras variaveis do tipo EditText que foram inseridas no layout")
    private lateinit var salvarButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        salvarButton = view.findViewById(R.id.salvarItemButton)
        enderecoEditText = view.findViewById(R.id.enderecoItemEditText)
        //TODO("Capture aqui os outro campos que foram inseridos no layout. Por exemplo, ate
        // o momento so foi capturado o endereco (EditText)")

        auth = FirebaseAuth.getInstance()

        salvarButton.setOnClickListener {
            salvarItem()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun salvarItem() {


        //TODO("Capture aqui o conteudo que esta nos outros editTexts que foram criados")
        val endereco = enderecoEditText.text.toString().trim()


        if (endereco.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT)
                .show()
            return
        }


        val item = Item(endereco)




        //TODO("Altere a raiz que sera criada no seu banco de dados do realtime database.
        // Renomeie a raiz itens")
        databaseReference = FirebaseDatabase.getInstance().getReference("itens")

        saveItemIntoDatabase(item)
    }

    private fun saveItemIntoDatabase(item: Item) {
        // Cria uma chave unica para o novo item
        val itemId = databaseReference.push().key
        if (itemId != null) {
            databaseReference.child(auth.uid.toString()).child(itemId).setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(context, "Item cadastrado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Falha ao cadastrar o item", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Erro ao gerar o ID do item", Toast.LENGTH_SHORT).show()
        }
    }
}