package db.repository;

import db.entity.Courier;
import db.entity.Item;
import db.entity.Restaurant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface RestaurantRepo extends CrudRepository<Restaurant, Integer> {

    @Query("SELECT r FROM Restaurant r left JOIN FETCH r.openingHours oh left Join fetch r.menu m WHERE r.name = ?1")
    Restaurant findByNameWithOpeningHoursAndMenu(String name);

    @Query("SELECT r FROM Restaurant r left Join fetch r.menu m WHERE r.name = ?1")
    Restaurant findByNameWithManu(String name);

    @Query("SELECT r.id FROM Restaurant r")
    List<Integer> getRestaurantsIds();

    @Query("SELECT c from Courier c")
    List<Courier> getCouriers();

    @Query("SELECT i FROM Restaurant r JOIN r.menu i WHERE r.id = ?1")
    List<Item> getMenu(int restaurantId);

    @Query("SELECT r FROM Restaurant r left JOIN FETCH r.openingHours oh left Join fetch r.menu m")
    Collection<Restaurant> getAllRestaurantsInfo();
}
