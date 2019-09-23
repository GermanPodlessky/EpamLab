package dao;

import bean.Characters.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DaoClient implements Dao<Client> {
    private static DaoClient ourInstance = new DaoClient();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    static DaoClient getInstance() {
        return ourInstance;
    }

    private List<Client> clients = new ArrayList<>();

    private DaoClient() {
    }

    @Override
    public void delete(Client obj) throws DaoException {

        Stream<Client> stream = clients.stream().filter(x -> x.equals(obj));

        if (stream.count() != 0) {
            clients.forEach(x -> clients.remove(obj));
            return;
        }

        throw new DaoException("This client isn't exist");
    }

    @Override
    public void add(Client obj) throws DaoException {
        if (obj == null) {
            throw new DaoException("Client is null");
        }

        if (clients.stream().anyMatch(x -> x.equals(obj))) {
            throw new DaoException("This client in exist");
        }

        clients.add(obj);
    }

    @Override
    public Client get(int id) throws DaoException {
        return clients.stream().filter(x -> x.getId() == id).findFirst()
                .orElseThrow(() -> new DaoException("This client is not exist"));
    }

    @Override
    public void addAll(List<Client> items) throws DaoException {
        if (items == null) {
            throw new DaoException("Clients is null");
        }

        Stream<Client> clients = this.clients.stream();
        if (items.stream().anyMatch(x -> clients.anyMatch(z -> z.equals(x)))) {
            throw new DaoException("Some of this obj is exist");
        }

        this.clients.addAll(items);
    }

    /**
     * Gets client list.
     *
     * @return the client list
     */
    public List<Client> getClients() {
        return clients;
    }
}