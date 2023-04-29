package my.edu.tarc.contact

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.contact.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), MenuProvider, RecordClickListener {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Refers to the ViewModel created by the Main Activity
    private val myContactViewModel: ContactViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

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

        val adapter = ContactAdapter(this)

        // Add an observer
        myContactViewModel.contactList.observe(
            viewLifecycleOwner,
            Observer {
                if (it.isEmpty()) {
                    binding.textViewCount.isVisible = true
                    binding.textViewCount.text = getString(R.string.no_record)
                } else {
                    binding.textViewCount.isVisible = false
                }
                adapter.setContact(it)
            }
        )
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        //menuInflater.inflate(R.menu.menu_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_upload) {
            if (myContactViewModel.contactList.isInitialized) {
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                val userRef = sharedPref.getString(getString(R.string.phone), "")
                if (userRef.isNullOrEmpty()) {
                    Toast.makeText(context, getString(R.string.error_profile), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val database =
                        Firebase.database("https://contact-d5c9c-default-rtdb.asia-southeast1.firebasedatabase.app").reference
                    myContactViewModel.contactList.value!!.forEach {
                        database.child(userRef).child(it.phone).setValue(it)
                    }
                }
            }
        }
        return true
    }

    override fun onRecordClickListener(index: Int) {
        myContactViewModel.selectedIndex = index
        findNavController().navigate(R.id.nav_second)
    }
}