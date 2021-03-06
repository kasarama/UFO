package db.repository;

import db.entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@TestPropertySource(locations = {"classpath:test.properties"})
public class DataBaseIntegrationTest {

    @Autowired
    private RestaurantRepo repoRest;
    @Autowired
    private ItemRepo repoItem;
    @Autowired
    private OrderRepo repoOrder;
    @Autowired
    private AddressRepo repoAddress;
    @Autowired
    private ZipcodeRepo repoZipcode;
    @Autowired
    private CourierRepo repoCourier;
    @Autowired
    private CustomerRepo repoCustomer;
    @Autowired
    private RoleRepo repoRole;
    @Autowired
    private UserRepo repoUser;

    private int initAddressId;
    private int initCourierId;
    private int initCustomerId;
    private int initUserid;
    private List<Role> roles = new ArrayList();

    @BeforeEach
    void setup() {
        Item item = new Item("Boler i Karry", "middag", "Svinekøds boller", 59.0);
        Set<Item> menu = new HashSet<>();
        menu.add(item);

        Set<OpeningHours> openingHours = new HashSet();
        openingHours.add(new OpeningHours("Monday", "10:00", "22:00"));
        openingHours.add(new OpeningHours("Tuesday", "10:00", "22:00"));

        Zipcode zipcode = new Zipcode(2860, "Hovedstaden", "Gladsaxe", "Søborg");
        Zipcode zipcode2 = new Zipcode(2860, "Hovedstaden", "Gladsaxe", "Bagsværd");
        Address address = new Address("Buddingevej", "122", "st.th", 2860, 12.11, 12.11);
        repoZipcode.save(zipcode);
        repoZipcode.save(zipcode2);
        initAddressId = repoAddress.save(address).getId();
        Restaurant restaurant = new Restaurant("Pizza Mama Mia", menu, openingHours);
        repoRest.save(restaurant);
        Courier courier = new Courier("Magdalena", "Wawrzak", 454.553, 7465343.5);
        initCourierId = repoCourier.save(courier).getId();
        Customer customer = new Customer("Jean", "Poul");
        initCustomerId = repoCustomer.save(customer).getId();
        Role rCustomer = new Role("Customer");
        Role rCourier = new Role("Courier");
        Role rRestaur = new Role("Restaurateur");

        roles.add(repoRole.save(rCustomer));
        roles.add(repoRole.save(rCourier));
        roles.add(repoRole.save(rRestaur));
        initUserid = repoUser.save(new User("Nina@mial.com", "Strong", "call me", address)).getId();

        System.out.println("Setup finished");
    }

    @AfterEach
    void cleanup() {
        repoOrder.deleteAll();
        repoItem.deleteAll();
        repoRest.deleteAll();
        repoCourier.deleteAll();
        repoCustomer.deleteAll();
        repoUser.deleteAll();
        repoRole.deleteAll();
        repoAddress.deleteAll();
        repoZipcode.deleteAll();
        repoUser.deleteAll();
        System.out.println("Cleaned up");
    }

    @Test
    public void saveRestaurantTes() {
        assertEquals(1, repoRest.count());
    }

    @Test
    void gateMenuTest() {
        Restaurant r = repoRest.findByNameWithOpeningHoursAndMenu("Pizza Mama Mia");
        assertEquals(1, r.getMenu().size());
    }

    @Test
    void getOpeningHoursTest() {
        for (OpeningHours o : repoRest.findByNameWithOpeningHoursAndMenu("Pizza Mama Mia").getOpeningHours()) {
            System.out.println(o.toString());
        }
        assertEquals(2, repoRest.findByNameWithOpeningHoursAndMenu("Pizza Mama Mia").getOpeningHours().size());
    }

    @Test
    void saveOrder() {
        Restaurant restaurant = repoRest.findByNameWithOpeningHoursAndMenu("Pizza Mama Mia");
        Item item = (Item) restaurant.getMenu().stream().toArray()[0];
        Order order = new Order(restaurant, null);
        order.addItem(item);
        order.addItem(item);
        order.addItem(item);

        repoOrder.save(order);

        Order savedOrder = repoOrder.findById(1).get();
        System.out.println("ITEMS ON THE LIST: " + savedOrder.getItems().keySet().size());
        Restaurant saved = savedOrder.getRestaurant();
        try {
            System.out.println(saved.getOpeningHours().toArray()[0].toString());
        } catch (Exception e) {
            System.out.println("Because we use a lazy fetch, opening hours are not loaded here. ");
        }
        assertEquals(59.0 * 3, savedOrder.getTotalPrice());
        assertEquals(restaurant.getId(), saved.getId());
    }

    @Test
    public void getAddressesZipcodeInfo() {
        assertEquals(2, repoZipcode.getForAddress(2860).size());
        //assertEquals(2860, repoAddress.findById(initAddressId).get().getZipcode().getZipcode());
    }


    @Test
    public void sendOrderWithCourier() {
        Restaurant restaurant = repoRest.findByNameWithOpeningHoursAndMenu("Pizza Mama Mia");
        Item item = (Item) restaurant.getMenu().stream().toArray()[0];
        Order order = new Order(restaurant, null);
        Order newOrder = repoOrder.save(order);
        Courier courier = repoCourier.findById(initCourierId).get();
        newOrder.sendWithCourier(courier);
        assertEquals("Magdalena", repoOrder.save(newOrder).getCourier().getFirstName());
    }

    @Test
    public void makeOrderAsCustomer() {
        Restaurant restaurant = repoRest.findByNameWithOpeningHoursAndMenu("Pizza Mama Mia");
        Item item = (Item) restaurant.getMenu().stream().toArray()[0];
        Customer customer = repoCustomer.findById(initCustomerId).get();
        Order order = new Order(restaurant, customer);
        Order newOrder = repoOrder.save(order);
        Courier courier = repoCourier.findById(initCourierId).get();
        newOrder.sendWithCourier(courier);
        assertEquals("Jean", repoOrder.save(newOrder).getCustomer().getFirstName());
    }

    @Test
    void saveUserWithMultipleRoles() {
        assertEquals(roles.size(), 3);
        User user = repoUser.findById(initUserid).get();

        user.addRole(roles.get(0));
        user.addRole(roles.get(1));
        user.addRole(roles.get(2));
        initUserid = repoUser.save(user).getId();
        User saved = repoUser.findById(initUserid).get();
        assertEquals(3, saved.getRoles().size());
        Customer customer = repoCustomer.findById(initCustomerId).get();
        customer.setUser(user);
        Courier courier = repoCourier.findById(initCourierId).get();
        courier.setUser(user);
        Restaurant restaurant = repoRest.findByNameWithOpeningHoursAndMenu("Pizza Mama Mia");
        restaurant.setUser(user);
        courier = repoCourier.save(courier);
        customer = repoCustomer.save(customer);
        restaurant = repoRest.save(restaurant);
        assertTrue(customer.getUser().getId() == courier.getUser().getId()
                && restaurant.getUser().getId() == saved.getId()
                && restaurant.getUser().getId() == customer.getUser().getId());
    }

    @Test
    public void getRestaurantsZipcode() {
        User user = repoUser.findById(initUserid).get();
        assertEquals(2860, user.getAddress().getZipcode());
    }

    @Test
    void findByNameWithManu() {
        Restaurant restaurant = repoRest.findByNameWithManu("Pizza Mama Mia");
        assertEquals(1, restaurant.getMenu().size());

    }
}
