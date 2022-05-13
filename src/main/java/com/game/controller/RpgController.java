package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.RpgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
public class RpgController {
    @Autowired
    private RpgService rpgService;

    @GetMapping("/rest/players")
    public List<Player> getListPlayer(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Race race,
            @RequestParam(required = false) Profession profession,
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Boolean banned,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(required = false) Integer maxLevel,
            @RequestParam(required = false) PlayerOrder order,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize
    ) {
        List<Player> players = rpgService.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience,
                minLevel, maxLevel);
        rpgService.sortPlayers(players, order);
        players = rpgService.getPage(players, pageNumber, pageSize);
        return players;
    }

    @GetMapping(path = "/rest/players/count")
    public Integer getShipsCount(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Race race,
            @RequestParam(required = false) Profession profession,
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Boolean banned,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(required = false) Integer maxLevel
    ) {
        return rpgService.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience,
                minLevel, maxLevel).size();
    }

    @PostMapping("/rest/players")
    public ResponseEntity<Player> addNewPlayer(@RequestBody @NonNull Player player) {
        if (!rpgService.validPlayer(player)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (player.getBanned() == null) {
            player.setBanned(false);
        }
        Player savedPlayer = rpgService.createPlayer(player);
        return new ResponseEntity<>(savedPlayer, HttpStatus.OK);
    }

    @GetMapping("/rest/players/{id}")
    public ResponseEntity<Player> getPlayerFromId(@PathVariable String id) {
        long idPlayer;
        try {
            idPlayer = Long.parseLong(id);
            if (idPlayer == 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        Player player = rpgService.getPlayerFromId(idPlayer);
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(player, HttpStatus.OK);
        }
    }

    @PostMapping("/rest/players/{id}")
    public ResponseEntity<Player> updatePlayer(
            @PathVariable String id,
            @RequestBody Player player
    ) {
        ResponseEntity<Player> entityPlayer = getPlayerFromId(id);
        Player oldPlayer = entityPlayer.getBody();
        if (oldPlayer ==  null) {
            return entityPlayer;
        }
        try {
            Player sendPlayer = rpgService.updatePlayer(oldPlayer, player);
            return new ResponseEntity<>(sendPlayer, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @DeleteMapping("/rest/players/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable String id) {
        ResponseEntity<Player> playerEntity = getPlayerFromId(id);
        Player player = playerEntity.getBody();
        if (player == null) {
            return playerEntity;
        }
        rpgService.deletePlayer(player);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
