package db.rest;

import db.entity.Restaurant;
import db.services.OrderPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    OrderPopulator populator;

    @GetMapping("/orders/populate")
    public String getById(@RequestParam("quantity") int quantity) {
        return "Total amount of orders in database : " + populator.populateOrders(quantity);
    }

    @GetMapping("/restaurants/info")
    public Collection<Restaurant> getAllRestaurantsInfo() {

        return populator.getRestaurantInfo();
    }
}
