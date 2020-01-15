/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.hook.temp.packages;

import java.util.Date;

import lombok.Getter;
import net.avicus.hook.Main;
import net.avicus.hook.wrapper.HookClient;
import net.avicus.magma.database.model.impl.CreditTransaction;

public abstract class /*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package{

@Getter
private final int price;
private final String successMessage;

public Package(int price,String successMessage){
        this.price=price;
        this.successMessage=successMessage;
        }

public void charge(HookClient client){
        Main.getExecutor().execute(()->{
        int amount=-Math.abs(this.price);
        CreditTransaction transaction=new CreditTransaction(client.getUser().getId(),amount,1.0,
        new Date());
        client.message("Yoy have been charged [b]"+this.price+"[/b] credits!");
        Main.getHook().getDatabase().getCreditTransactions().insert(transaction).execute();
        client.message(this.successMessage);
        });
        }

public void purchase(HookClient client){
        charge(client);
        }
        }
