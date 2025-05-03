# roulette_game_-_castle
# Roulette Strategies: Castle Method and Opposite Color Method

This repository explains two roulette strategies: the **Castle Method** and the **Opposite Color Method**. These strategies are designed to manage bets in a systematic way, aiming to optimize gains and minimize losses.

## Castle Method 01

The Castle Method involves placing bets on specific sectors of the roulette table. Here's how it works:

- **Bet 1 EUR** (or 0.50 EUR) on **0**.
- **Bet 5 EUR** on the **"1st 12"** sector.
- **Bet 5 EUR** on the **"2nd 12"** sector.
- Only if possible, **bet 0.50 EUR** on the **"3rd 12"** sector, straddling the two pairs at the bottom **"25 and 28"** and **"31 and 34"** (0.50 EUR and 0.50 EUR respectively).

In total, without the horses, **11.00 EUR** is staked per round using this method.

## Castle Method 02

The Castle Method involves placing bets on specific sectors of the roulette table. Here's how it works:

- **Bet 1 EUR** (or 0.50 EUR) on **0**.
- **Bet 5 EUR** on the **"2nd 12"** sector.
- **Bet 5 EUR** on the **"3rd 12"** sector.
- Only if possible, **bet 0.50 EUR** on the **"1st 12"** sector, straddling the two pairs at the bottom **"1 and 4"** and **"7 and 10"** (0.50 EUR and 0.50 EUR respectively).

In total, without the horses, **11.00 EUR** is staked per round using this method.

## Opposite Color Method

The **Opposite Color Method** is activated in the event of a loss with the **Castle Method**, specifically when the gain from the Castle Method decreases the total gain despite a victory.

Here's how it works:

- **Bet 8.00 EUR** on the color **opposite** to that of the number that was drawn just before. 
  - For example, if a red number belonging to the **"3rd 12"** sector was drawn, you would now bet on the opposite color, i.e., **black**.

After applying the Opposite Color Method, if you win, the Castle Method is applied again and the cycle continues.

## Video Explanation

For a more detailed explanation, you can watch the following video:  
[![Video Explanation](https://img.youtube.com/vi/VPmbUqGtrOY/0.jpg)](https://www.youtube.com/watch?v=VPmbUqGtrOY)

---

Feel free to explore and adapt these strategies for your own use. Good luck!

---

Castle Method 01 screenshot
![Png](https://i.ibb.co/q3ZpSYDj/Immagine-2025-04-10-222432.png)

Castle Method 02 screenshot
![Png](https://i.ibb.co/qLkmshhP/Immagine-2025-04-16-234336.png)

## Customizations

In the application, horses are not bet because some roulettes do not allow it.
If it is possible to bet on the horses, the horses are placed in the first dozen, rather than the third.

The "Opposite Color" strategy is applied dynamically based on the result of each individual roll, and can be used several consecutive times in both methods (Method 01 and Method 02).
The "Opposite Color" strategy is activated whenever the "Castle" strategy suffers a loss and remains active until a win occurs. This mechanism is identical for both Method 01 and Method 02, as the logic of changing strategy does not depend on the type of bet made in Castle mode.
