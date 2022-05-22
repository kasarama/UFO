package db.repository;

import db.entity.Order;
import db.entity.Restaurant;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepo extends CrudRepository<Order, Integer> {

   // Set<Restaurant>
}
