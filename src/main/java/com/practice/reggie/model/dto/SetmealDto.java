package com.practice.reggie.model.dto;

import com.practice.reggie.model.Setmeal;
import com.practice.reggie.model.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
