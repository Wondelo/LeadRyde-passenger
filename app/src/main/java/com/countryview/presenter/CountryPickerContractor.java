package com.countryview.presenter;



import com.countryview.model.Country;
import com.countryview.view.CountryPicker;

import java.util.List;

public interface CountryPickerContractor {

    interface View {

        void setCountries(List<Country> countries);
    }

    interface Presenter {


        void filterSearch(String query);

       // void sort(CountryPicker.Sort sort);
    }
}
