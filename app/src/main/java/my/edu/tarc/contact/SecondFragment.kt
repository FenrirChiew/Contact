package my.edu.tarc.contact

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import my.edu.tarc.contact.databinding.FragmentSecondBinding

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.second_menu, menu)
        menu.findItem(R.id.action_settings).isVisible = false
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
                }
                Toast.makeText(context, getString(R.string.contact_saved), Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (menuItem.itemId == android.R.id.home) {
            findNavController().navigateUp()
        }
        return true
    }

}