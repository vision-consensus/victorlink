package com.vision.web.controller;

import com.vision.job.adapters.FootballAdapter;
import com.vision.web.entity.FootBall;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.vision.common.runtime.vm.DataWord;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/football")
@AllArgsConstructor
public class FootBallController {

    @PostMapping("/create")
    public String create(@RequestBody FootBall footBall){
        BigInteger matchId = new BigInteger(footBall.getMatchId().getBytes(StandardCharsets.UTF_8));
        FootballAdapter.matchId = footBall.getMatchId();
        return String.valueOf(matchId.longValue());
    }


}
