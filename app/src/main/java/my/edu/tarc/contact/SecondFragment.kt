package my.edu.tarc.contact

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import my.edu.tarc.contact.databinding.FragmentSecondBinding
import my.edu.tarc.mycontact.WebDB
import org.json.JSONObject
import java.lang.Exception

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), MenuProvider {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Refers to the ViewModel created by the Main Activity
    private val myContactViewModel: ContactViewModel by activityViewModels()
    private var isEditing: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        //Let ProfileFragment to manage the Menu
        val menuHost: MenuHost = this.requireActivity()
        menuHost.addMenuProvider(
            this, viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Determine the view mode; edit or new
        isEditing = myContactViewModel.selectedIndex != -1
        if (isEditing) {
            with(binding) {
                val contact: Contact =
                    myContactViewModel.contactList.value!!.get(myContactViewModel.selectedIndex)
                editTextName.setText(contact.name)
                editTextPhone.setText(contact.phone)
                editTextName.requestFocus()
                editTextPhone.isEnabled = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        myContactViewModel.selectedIndex = -1
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.second_menu, menu)
        //menu.findItem(R.id.action_settings).isVisible = false
        //menu.findItem(R.id.action_delete).isVisible = isEditing
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_save) {
            if (binding.editTextName.text.isEmpty()) {
                Toast.makeText(context, getString(R.string.missing_name), Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.editTextPhone.text.isEmpty()) {
                Toast.makeText(context, getString(R.string.missing_phone), Toast.LENGTH_SHORT)
                    .show()
            } else {
                // TODO: Insert a new contact to the DB
                binding.apply {
                    val name = editTextName.text.toString()
                    val phone = editTextPhone.text.toString()
                    val newContact = Contact(name, phone)
                    myContactViewModel.addContact(newContact)
                    if (isEditing) {
                        myContactViewModel.updateContact(newContact)
                    } else {
                        myContactViewModel.addContact(newContact)
                    }
                }
                Toast.makeText(context, getString(R.string.contact_saved), Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }
        } else if (menuItem.itemId == R.id.action_delete) {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setMessage(getString(R.string.delete_record))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    val contact =
                        myContactViewModel.contactList.value!!.get(myContactViewModel.selectedIndex)
                    myContactViewModel.deleteContact(contact)
                    findNavController().navigateUp()
                }.setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    // DO NOTHING HERE
                }
            builder.create().show()
        } else if (menuItem.itemId == android.R.id.home) {
            findNavController().navigateUp()
        }
        return true
    }

    private fun createUser(contact: Contact) {
        val url =
            getString(R.string.url_server) + getString(R.string.url_insert) + "?name=" + contact.name + "&contact=" + contact.phone
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    if (response != null) {
                        val strResponse = response.toString()
                        val jsonResponse = JSONObject(strResponse)
                        val success: String = jsonResponse.get("success").toString()


                        if (success.equals("1")) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.contact_saved),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.contact_not_saved),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("Second Fragment", "Response: %s".format(e.message.toString()))
                }
            },
            { error ->
                Log.d("Second Fragment", "Response : %s".format(error.message.toString()))
            }
        )
        jsonObjectRequest.retryPolicy =
            DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 1f)
        WebDB.getInstance(requireContext()).addToRequestQueue(jsonObjectRequest)
    }
}