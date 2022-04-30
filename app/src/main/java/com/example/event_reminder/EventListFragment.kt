package com.example.event_reminder

import android.app.DatePickerDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.event_reminder.adapters.ItemAdapter
import com.example.event_reminder.util.ActionType
import com.example.event_reminder.util.ErrorType
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EventListFragment : Fragment() {

    companion object {
        fun newInstance(position: Int): EventListFragment {
            val eventListFragment = EventListFragment()
            val args = Bundle()
            args.putInt("month", position)
            eventListFragment.arguments = args
            return eventListFragment
        }
    }

    private lateinit var viewModel: EventListViewModel
    private lateinit var adapter: ItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val addEventButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        viewModel = ViewModelProvider(requireActivity()).get(EventListViewModel::class.java)

        viewModel.month = arguments?.getInt("month", 0)!!
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ItemAdapter(requireContext(), viewModel.getEventList())
        recyclerView.adapter = adapter

        adapter.onItemClick = { event ->
            showBottomSheetDialog(ActionType.UPDATE, event)
        }

        addEventButton.setOnClickListener {
            showBottomSheetDialog(ActionType.ADD, null)
        }

    }

    override fun onResume() {
        super.onResume()
        //setting view model's month to current tab
        viewModel.month = arguments?.getInt("month", 0)!!
        //notifying the adapter of change
        if (this::adapter.isInitialized && this::viewModel.isInitialized)
            adapter.notifyDataSetChanged()

    }

    /**
     * Shows bottom sheet for ADD and UPDATE event functionalities
     */
    private fun showBottomSheetDialog(actionType: ActionType, event: Event?) {
        //variables to store the date chosen by user using date picker
        var day: Int = 0
        var month: Int = 0
        var year: Int = 0

        val bottomSheet: BottomSheetDialog =
            BottomSheetDialog(this.requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheet.setContentView(R.layout.bottom_sheet_addevent)
        val addButton: Button? = bottomSheet.findViewById(R.id.btn_add)
        val nameTextView: TextView? = bottomSheet.findViewById(R.id.edt_name)
        val dateTextView: TextView? = bottomSheet.findViewById(R.id.edt_date)
        val deleteImage: ImageView? = bottomSheet.findViewById(R.id.imv_delete)
        val radioGroup: RadioGroup? = bottomSheet.findViewById(R.id.radioGroup)
        //if the user is updating an existing event
        if (actionType == ActionType.UPDATE) {
            val date: String = "${event?.day}-${event?.month}-${event?.year}"
            addButton?.text = getString(R.string.update)
            nameTextView?.text = event?.name
            dateTextView?.text = date
            deleteImage?.visibility = View.VISIBLE
            //checking appropriate radio button according to event's type
            if (event?.eventType == 1)
                radioGroup?.check(R.id.anniversaryRadioButton)
            else
                radioGroup?.check(R.id.birthdayRadioButton)
        }
        dateTextView?.setOnClickListener {
            val datePicker = DatePickerDialog(this.requireContext(), R.style.DialogTheme)
            //setting event's date in date picker
            if (actionType == ActionType.UPDATE) {
                datePicker.updateDate(event?.year!!, event.month!! - 1, event.day!!)
            }
            datePicker.setOnDateSetListener { _, _year, _month, _day ->
                day = _day
                month = _month
                year = _year
                val date: String = "${_day}-${_month + 1}-${_year}"
                dateTextView.text = date
            }
            datePicker.show()
        }
        addButton?.setOnClickListener {
            //getting event type from radio buttons
            val currentEventType: Int = when (radioGroup?.checkedRadioButtonId) {
                R.id.birthdayRadioButton -> 0
                R.id.anniversaryRadioButton -> 1
                else -> -1
            }
            if (actionType == ActionType.ADD) {
                //validate the added event
                val validator: Pair<Boolean, ErrorType> = viewModel.validateAddedEvent(
                    nameTextView?.text.toString(),
                    day,
                    month + 1,
                    year,
                    currentEventType
                )
                // if the event is validated then add it
                if (validator.first) {
                    viewModel.addEvent(
                        nameTextView?.text.toString(),
                        day,
                        month + 1,
                        year,
                        currentEventType
                    )
                    //if event is added to the current tab then notify the adapter
                    if (month + 1 == viewModel.month) {
                        adapter.notifyItemInserted(viewModel.getEventListSize())
                    }
                    bottomSheet.dismiss()
                // if event is not validated then show appropriate error message
                } else Toast.makeText(context, getErrorString(validator.second), Toast.LENGTH_SHORT)
                    .show()
            } else {
                //get the index of the event to be updated
                var updateIndex: Int = 0
                if (event?.month == viewModel.month)
                    updateIndex = viewModel.getEventPosition(event)
                /*if date of the event has not been changed then set the variables to the event's
                 original date*/
                if (day == 0) {
                    day = event?.day!!
                    month = event.month!! - 1
                    year = event.year!!
                }
                //get the event's original month before updating
                var prevMonth: Int? = event?.month
                //validate the updated event
                val validator: Pair<Boolean, ErrorType> = viewModel.validateUpdatedEvent(
                    event?.name!!,
                    nameTextView?.text.toString(),
                    day,
                    month + 1,
                    year,
                    currentEventType
                )
                //if the event is validated then add it
                if (validator.first) {
                    viewModel.updateEvent(
                        event?.name!!,
                        nameTextView?.text.toString(),
                        day,
                        month + 1,
                        year,
                        currentEventType
                    )
                    //if event updated is of the current tab then notify the adapter
                    if (prevMonth == viewModel.month)
                    //optimize this
                        adapter.notifyDataSetChanged()
                    bottomSheet.dismiss()
                //if event is not validated then show appropriate error message
                } else Toast.makeText(context, getErrorString(validator.second), Toast.LENGTH_SHORT)
                    .show()

            }
        }
        deleteImage?.setOnClickListener {
            var deleteIndex: Int = 0
            val eventMonth: Int? = event?.month
            // get index of the deleted event if it is on the current tab
            if (event?.month == viewModel.month)
                deleteIndex = viewModel.getEventPosition(event)

            viewModel.deleteEvent(event!!)
            //if event deleted is of the current tab notify the adapter
            if (eventMonth == viewModel.month)
            //optimize this
                adapter.notifyItemRemoved(deleteIndex)
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }

    /**
     * Returns error message according to Error Type
     */
    private fun getErrorString(errorType: ErrorType): String {
        val errorString = when (errorType) {
            ErrorType.CHOOSE_DATE -> getString(R.string.choose_date_error)
            ErrorType.CHOOSE_EVENT_TYPE -> getString(R.string.choose_event_type_error)
            ErrorType.NAME_BLANK -> getString(R.string.name_blank_error)
            ErrorType.DATE_IN_FUTURE -> getString(R.string.date_in_future_error)
            ErrorType.NAME_UNIQUE -> getString(R.string.name_unique_error)
            else -> getString(R.string.no_error)
        }
        return errorString
    }
}