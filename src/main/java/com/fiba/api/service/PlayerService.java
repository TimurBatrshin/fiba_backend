package com.fiba.api.service;

import com.fiba.api.model.Player;
import com.fiba.api.model.User;
import com.fiba.api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Transactional
    public Player createOrUpdatePlayer(User user) {
        return playerRepository.findByName(user.getUsername())
            .orElseGet(() -> {
                Player player = new Player();
                player.setName(user.getUsername());
                return playerRepository.save(player);
            });
    }

    @Transactional
    public List<Player> getOrCreatePlayers(List<User> users) {
        return users.stream()
            .map(this::createOrUpdatePlayer)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }
} 