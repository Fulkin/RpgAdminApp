package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class RpgServiceImpl implements RpgService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public List<Player> getPlayers(String name, String title, Race race, Profession profession,
                                   Long after, Long before, Boolean banned, Integer minExperience,
                                   Integer maxExperience, Integer minLevel, Integer maxLevel) {
        Date afterDate = after == null ? null : new Date(after);
        Date beforeDate = before == null ? null : new Date(before);
        final List<Player> players = new ArrayList<>();

        playerRepository.findAll().forEach(player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;

            players.add(player);
        });

        return players;
    }

    @Override
    public void sortPlayers(List<Player> players, PlayerOrder order) {
        if (order != null && order != PlayerOrder.ID) {
            players.sort((first, second) -> {
                switch (order) {
                    case NAME:
                        return first.getName().compareTo(second.getName());
                    case LEVEL:
                        return first.getLevel().compareTo(second.getLevel());
                    case BIRTHDAY:
                        return first.getBirthday().compareTo(second.getBirthday());
                    case EXPERIENCE:
                        return first.getExperience().compareTo(second.getExperience());
                    default:
                        return 0;
                }
            });
        }
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final int page = pageNumber == null ? 0 : pageNumber;
        final int size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        return players.subList(from, Math.min(to, players.size()));
    }

    @Override
    public Player createPlayer(Player player) {
        setLevelAndExpForNextLvl(player);
        return playerRepository.save(player);
    }

    @Override
    public boolean validPlayer(Player player) {
        return player != null &&
                isValidName(player.getName()) &&
                isValidTitle(player.getTitle()) &&
                isValidRaceAndProfession(player.getRace(), player.getProfession()) &&
                isValidExperience(player.getExperience()) &&
                isValidBirthday(player.getBirthday());
    }

    @Override
    public Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalArgumentException{
        String name = newPlayer.getName();
        if (name != null) {
            if (isValidName(name)) {
                oldPlayer.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        String title = newPlayer.getTitle();
        if (title != null) {
            if (isValidTitle(title)) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }

        Date birthday = newPlayer.getBirthday();
        if (birthday != null) {
            if (isValidBirthday(birthday)) {
                oldPlayer.setBirthday(birthday);
            } else {
                throw new IllegalArgumentException();
            }
        }

        Boolean banned = newPlayer.getBanned();
        if (banned != null) {
            oldPlayer.setBanned(banned);
        }

        Integer experience = newPlayer.getExperience();
        if (experience != null) {
            if (isValidExperience(experience)) {
                oldPlayer.setExperience(experience);
                setLevelAndExpForNextLvl(oldPlayer);
            } else {
                throw new IllegalArgumentException();
            }
        }

        return oldPlayer;
    }

    @Override
    public Player getPlayerFromId(Long idPlayer) {
        return playerRepository.findById(idPlayer).orElse(null);
    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    private boolean isValidName(String name) {
        return name != null && name.length() <= 12 && !name.isEmpty();
    }

    private boolean isValidTitle(String title) {
        return title != null && title.length() <= 30;
    }

    private boolean isValidRaceAndProfession(Race race, Profession profession) {
        return race != null && profession != null;
    }

    private boolean isValidExperience(Integer experience) {
        return experience != null &&
                experience >= 0 && experience <= 10_000_000;
    }

    private boolean isValidBirthday(Date birthday) {
        Calendar calendar2000 = Calendar.getInstance();
        calendar2000.set(Calendar.YEAR, 2000);
        Calendar calendar3000 = Calendar.getInstance();
        calendar3000.set(Calendar.YEAR, 3000);
        return birthday != null && birthday.getTime() > 0 &&
                birthday.after(calendar2000.getTime()) && birthday.before(calendar3000.getTime());
    }

    private void setLevelAndExpForNextLvl(Player player) {
        Integer experience = player.getExperience();
        double levelD = (Math.sqrt(2500 + 200 * experience) - 50) / 100;
        int level = (int) levelD;
        Integer expForNextLvl = 50 * (level + 1) * (level + 2) - experience;
        player.setLevel(level);
        player.setUntilNextLevel(expForNextLvl);
    }
}
