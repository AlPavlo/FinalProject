package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.os.Handler;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    int ghp = 100;
    int zhp = 70;  // Здоровье зомби увеличено
    private int combo_step = 1;
    private int zombieStep = 1;
    private Handler handler = new Handler();
    private Runnable runnable;
    String personage = null;
    ImageView gamer;
    JoystickView joystickView;
    ImageButton attackButton;
    private boolean isAttacking = false;
    private float currentXPercent = 0f;
    private float currentYPercent = 0f;
    private Random random = new Random();
    private long lastZombieHitTime = 0;
    private boolean zombieActive = false;
    int game = 0; // 0 - игра не активна, 1 - игра активна

    // Класс персонажа с параметрами, включая имя спрайта пули и размер пули
    class Character {
        String name;
        int damage;
        float bulletSpeed;
        float attackRange;
        String bulletSpriteName;  // имя спрайта пули
        int bulletWidthDp;
        int bulletHeightDp;

        Character(String name, int damage, float bulletSpeed, float attackRange, String bulletSpriteName, int bulletWidthDp, int bulletHeightDp) {
            this.name = name;
            this.damage = damage;
            this.bulletSpeed = bulletSpeed;
            this.attackRange = attackRange;
            this.bulletSpriteName = bulletSpriteName;
            this.bulletWidthDp = bulletWidthDp;
            this.bulletHeightDp = bulletHeightDp;
        }
    }

    // Персонажи с параметрами, именами спрайтов пули и размерами пули в dp
    Character musketeer = new Character("mushketeer", 10, 20f, 1000f, "bullet_mushketeer", 20, 20);
    Character karatel = new Character("karatel", 35, 5f, 100f, "bullet_karatel", 30, 60);
    Character boxer = new Character("boxer", 23, 15f, 200f, "bullet_boxer", 40, 30);
    Character steampunker = new Character("steampunker", 35, 3f, 500f, "bullet_steampunker", 40, 40);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTimer();

        ImageView fone = findViewById(R.id.fone);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView start = findViewById(R.id.start);
        ImageView BLOCK_tree = findViewById(R.id.BLOCK_tree);
        ImageView lobby = findViewById(R.id.lobby);
        ImageView BLOCK_freezer1 = findViewById(R.id.BLOCK_freezer1);
        ImageView BLOCK_freezer2 = findViewById(R.id.BLOCK_freezer2);
        ImageView BLOCK_taburet1 = findViewById(R.id.BLOCK_taburet1);
        ImageView BLOCK_taburet2 = findViewById(R.id.BLOCK_taburet2);
        ImageView BLOCK_truba = findViewById(R.id.BLOCK_truba);
        ImageView BLOCK_boxing = findViewById(R.id.BLOCK_boxing);
        ImageView BLOCK_redthing = findViewById(R.id.BLOCK_redthing);
        gamer = findViewById(R.id.gamer);
        ImageView boxerChar = findViewById(R.id.boxer);
        ImageView steampunkerChar = findViewById(R.id.steampunker);
        ImageView mushketeerChar = findViewById(R.id.mushketeer);
        ImageView karatelChar = findViewById(R.id.karatel);
        ImageView city = findViewById(R.id.city);
        ImageView portal = findViewById(R.id.portal);
        ImageView BLOCK_barrier = findViewById(R.id.BLOCK_barrier);
        ImageView BLOCK_barrier2 = findViewById(R.id.BLOCK_barrier2);
        ImageView zombie = findViewById(R.id.zombie);

        joystickView = findViewById(R.id.joystick_view);
        joystickView.setVisibility(View.GONE);
        attackButton = findViewById(R.id.Attack);
        attackButton.setVisibility(View.GONE);

        attackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attack();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.GONE);
                fone.setVisibility(View.GONE);
                lobby.setVisibility(View.VISIBLE);
                BLOCK_tree.setVisibility(View.VISIBLE);
                BLOCK_boxing.setVisibility(View.VISIBLE);
                BLOCK_freezer1.setVisibility(View.VISIBLE);
                BLOCK_freezer2.setVisibility(View.VISIBLE);
                BLOCK_redthing.setVisibility(View.VISIBLE);
                BLOCK_truba.setVisibility(View.VISIBLE);
                BLOCK_taburet1.setVisibility(View.VISIBLE);
                BLOCK_taburet2.setVisibility(View.VISIBLE);
                karatelChar.setVisibility(View.VISIBLE);
                boxerChar.setVisibility(View.VISIBLE);
                steampunkerChar.setVisibility(View.VISIBLE);
                mushketeerChar.setVisibility(View.VISIBLE);
                portal.setVisibility(View.VISIBLE);
            }
        });

        setCharacterClickListener(boxerChar, "boxer");
        setCharacterClickListener(steampunkerChar, "steampunker");
        setCharacterClickListener(mushketeerChar, "mushketeer");
        setCharacterClickListener(karatelChar, "karatel");

        joystickView.setJoystickListener(new JoystickView.JoystickListener() {
            @Override
            public void onJoystickMoved(float xPercent, float yPercent) {
                currentXPercent = xPercent;
                currentYPercent = yPercent;

                float speed = 10f;
                float currentX = gamer.getX() + xPercent * speed;
                float currentY = gamer.getY() + yPercent * speed;

                int parentWidth = ((View) gamer.getParent()).getWidth();
                int parentHeight = ((View) gamer.getParent()).getHeight();

                if (currentX < 0) currentX = 0;
                if (currentY < 0) currentY = 0;
                if (currentX > parentWidth - gamer.getWidth())
                    currentX = parentWidth - gamer.getWidth();
                if (currentY > parentHeight - gamer.getHeight())
                    currentY = parentHeight - gamer.getHeight();

                float prevX = gamer.getX();
                float prevY = gamer.getY();

                gamer.setX(currentX);
                gamer.setY(currentY);

                if (checkCollisionWithBlocks()) {
                    gamer.setX(prevX);
                    gamer.setY(prevY);
                }

                updateGamerSprite(currentXPercent, currentYPercent);
            }
        });
    }

    private void startTimer() {
        runnable = new Runnable() {
            @Override
            public void run() {
                combo_step = (combo_step == 1) ? 2 : 1;
                zombieStep = (zombieStep == 1) ? 2 : 1;
                handler.postDelayed(this, 250);
            }
        };
        handler.post(runnable);
    }

    private Character getCurrentCharacter() {
        switch (personage) {
            case "mushketeer":
                return musketeer;
            case "karatel":
                return karatel;
            case "boxer":
                return boxer;
            case "steampunker":
                return steampunker;
            default:
                return null;
        }
    }

    private void attack() {
        if (isAttacking) return;
        isAttacking = true;

        ImageView bullet = findViewById(R.id.Bullet);
        if (bullet != null) {
            Character character = getCurrentCharacter();
            if (character != null) {
                int bulletResId = getResources().getIdentifier(character.bulletSpriteName, "drawable", getPackageName());
                if (bulletResId != 0) {
                    bullet.setImageResource(bulletResId);
                }
                // Установка размера пули в пикселях из dp
                int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, character.bulletWidthDp, getResources().getDisplayMetrics());
                int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, character.bulletHeightDp, getResources().getDisplayMetrics());
                ViewGroup.LayoutParams params = bullet.getLayoutParams();
                params.width = widthPx;
                params.height = heightPx;
                bullet.setLayoutParams(params);

                bullet.setVisibility(View.VISIBLE);
                bullet.setX(gamer.getX() + gamer.getWidth() / 2f - widthPx / 2f);
                bullet.setY(gamer.getY() + gamer.getHeight() / 2f - heightPx / 2f);
                float angle = (float) Math.toDegrees(Math.atan2(currentYPercent, currentXPercent));
                bullet.setRotation(angle);

                moveBullet(bullet, currentXPercent, currentYPercent, character.bulletSpeed, character.attackRange, character.damage);
            } else {
                bullet.setVisibility(View.GONE);
                isAttacking = false; // сброс флага если персонаж не выбран
            }
        } else {
            isAttacking = false; // сброс флага если пули нет
        }
    }

    private void moveBullet(final ImageView bullet, final float dirX, final float dirY, final float speed, final float maxRange, final int damage) {
        final float distanceStep = speed;
        final float[] distanceTravelled = {0};

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bullet.getVisibility() != View.VISIBLE) {
                    isAttacking = false; // сброс флага при исчезновении пули
                    return;
                }

                bullet.setX(bullet.getX() + dirX * speed);
                bullet.setY(bullet.getY() + dirY * speed);

                distanceTravelled[0] += distanceStep;
                if (checkBulletCollisionWithBlocks(bullet)) {
                    bullet.setVisibility(View.GONE);
                    isAttacking = false; // сброс флага при уничтожении пули преградой
                    return;
                }

                checkBulletCollision(bullet, damage);
                if (distanceTravelled[0] >= maxRange || bullet.getVisibility() != View.VISIBLE) {
                    bullet.setVisibility(View.GONE);
                    isAttacking = false; // сброс флага при достижении максимального расстояния или уничтожении при столкновении
                    return;
                }

                handler.postDelayed(this, 16);
            }
        }, 16);
    }

    // Проверка, есть ли блок на пути между пулей и зомби (преграда для пули)
    private boolean checkBulletCollisionWithBlocks(ImageView bullet) {
        ImageView zombie = findViewById(R.id.zombie);
        if (zombie.getVisibility() != View.VISIBLE) return false;

        float bulletCenterX = bullet.getX() + bullet.getWidth() / 2f;
        float bulletCenterY = bullet.getY() + bullet.getHeight() / 2f;
        float zombieCenterX = zombie.getX() + zombie.getWidth() / 2f;
        float zombieCenterY = zombie.getY() + zombie.getHeight() / 2f;

        return isLineObstructedByBlocks(bulletCenterX, bulletCenterY, zombieCenterX, zombieCenterY);
    }

    // Проверка пересечения линии с блоками
    private boolean isLineObstructedByBlocks(float startX, float startY, float endX, float endY) {
        ViewGroup parent = (ViewGroup) gamer.getParent();

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            String childName = getResources().getResourceEntryName(child.getId());
            if (childName.startsWith("BLOCK_")) {
                int blockWidth = child.getWidth();
                int blockHeight = child.getHeight();
                float blockX = child.getX();
                float blockY = child.getY();

                if (lineIntersectsRect(startX, startY, endX, endY, blockX, blockY, blockX + blockWidth, blockY + blockHeight)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Проверка пересечения линии и прямоугольника
    private boolean lineIntersectsRect(float x1, float y1, float x2, float y2, float left, float top, float right, float bottom) {
        // Проверяем пересечения линии с 4 сторонами прямоугольника
        if (lineIntersectsLine(x1, y1, x2, y2, left, top, right, top)) return true;      // top
        if (lineIntersectsLine(x1, y1, x2, y2, right, top, right, bottom)) return true;  // right
        if (lineIntersectsLine(x1, y1, x2, y2, left, bottom, right, bottom)) return true; // bottom
        if (lineIntersectsLine(x1, y1, x2, y2, left, top, left, bottom)) return true;    // left

        // Также проверяем, если линия целиком внутри прямоугольника (например, start или end внутри блока)
        if (pointInRect(x1, y1, left, top, right, bottom)) return true;
        if (pointInRect(x2, y2, left, top, right, bottom)) return true;

        return false;
    }

    private boolean pointInRect(float px, float py, float left, float top, float right, float bottom) {
        return (px >= left && px <= right && py >= top && py <= bottom);
    }

    // Проверка пересечения двух отрезков (линий)
    private boolean lineIntersectsLine(float x1, float y1, float x2, float y2,
                                       float x3, float y3, float x4, float y4) {
        float denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0f) {
            return false; // Параллельны
        }
        float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        return (ua >= 0f && ua <= 1f) && (ub >= 0f && ub <= 1f);
    }

    private void checkBulletCollision(ImageView bullet, int damage) {
        ImageView zombie = findViewById(R.id.zombie);
        if (zombie.getVisibility() != View.VISIBLE) return;

        int bulletWidth = bullet.getWidth();
        int bulletHeight = bullet.getHeight();
        float bulletX = bullet.getX();
        float bulletY = bullet.getY();

        int zombieWidth = zombie.getWidth();
        int zombieHeight = zombie.getHeight();
        float zombieX = zombie.getX();
        float zombieY = zombie.getY();

        if (bulletX < zombieX + zombieWidth && bulletX + bulletWidth > zombieX &&
                bulletY < zombieY + zombieHeight && bulletY + bulletHeight > zombieY) {

            zhp -= damage;
            bullet.setVisibility(View.GONE);
            isAttacking = false; // сброс флага по удару

            if (zhp <= 0) {
                zombie.setVisibility(View.GONE);
                zombieActive = false;
                zhp = 70; // Восстановление здоровья зомби после смерти
            }
        }
    }

    private void updateGamerSprite(float xPercent, float yPercent) {
        if (personage == null) return;
        Character character = getCurrentCharacter();
        String spriteNameBase = (character != null) ? character.name : personage;

        String spriteName = spriteNameBase + "_";

        if (isAttacking) {
            if (yPercent < -0.5) {
                spriteName += (combo_step == 1) ? "shotup1" : "shotup2";
                gamer.setScaleX(1f);
            } else if (yPercent > 0.5) {
                spriteName += (combo_step == 1) ? "shotdown1" : "shotdown2";
                gamer.setScaleX(1f);
            } else if (xPercent < -0.5) {
                spriteName += (combo_step == 1) ? "shotright1" : "shotright2";
                gamer.setScaleX(-1f);
            } else if (xPercent > 0.5) {
                spriteName += (combo_step == 1) ? "shotright1" : "shotright2";
                gamer.setScaleX(1f);
            } else {
                spriteName += "shotstart";
            }
        } else {
            if (yPercent < -0.5) {
                spriteName += (combo_step == 1) ? "up1" : "up2";
                gamer.setScaleX(1f);
            } else if (yPercent > 0.5) {
                spriteName += (combo_step == 1) ? "down1" : "down2";
                gamer.setScaleX(1f);
            } else if (xPercent < -0.5) {
                spriteName += (combo_step == 1) ? "right1" : "right2";
                gamer.setScaleX(-1f);
            } else if (xPercent > 0.5) {
                spriteName += (combo_step == 1) ? "right1" : "right2";
                gamer.setScaleX(1f);
            } else {
                spriteName += "start";
            }
        }
        spriteName += "_high";

        int resId = getResources().getIdentifier(spriteName, "drawable", getPackageName());
        if (resId != 0) gamer.setImageResource(resId);
    }

    private void setCharacterClickListener(final ImageView characterImage, final String characterName) {
        characterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (personage == null) {
                    personage = characterName;
                    characterImage.setVisibility(View.GONE);
                    joystickView.setVisibility(View.VISIBLE);
                    attackButton.setVisibility(View.VISIBLE);
                    moveGamerToCharacter(characterImage);
                }
            }
        });
    }

    private void moveGamerToCharacter(ImageView characterImage) {
        int[] charLocation = new int[2];
        characterImage.getLocationOnScreen(charLocation);
        int[] parentLocation = new int[2];
        ((View) gamer.getParent()).getLocationOnScreen(parentLocation);
        float x = charLocation[0] - parentLocation[0];
        float y = charLocation[1] - parentLocation[1];
        gamer.setX(x);
        gamer.setY(y);
        switch (personage) {
            case "boxer":
                gamer.setImageResource(R.drawable.boxer_start_high);
                break;
            case "steampunker":
                gamer.setImageResource(R.drawable.steampunker_start_high);
                break;
            case "mushketeer":
                gamer.setImageResource(R.drawable.mushketeer_start_high);
                break;
            case "karatel":
                gamer.setImageResource(R.drawable.karatel_start_high);
                break;
        }
        gamer.setVisibility(View.VISIBLE);
    }

    private void spawnZombie() {
        if (zombieActive) return;
        if (game != 1) return; // Спавним зомби только если игра активна (game == 1)

        ImageView zombie = findViewById(R.id.zombie);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        int edge = random.nextInt(4);
        float x, y;
        switch (edge) {
            case 0:
                x = random.nextInt(screenWidth);
                y = 0;
                break;
            case 1:
                x = random.nextInt(screenWidth);
                y = screenHeight - zombie.getHeight();
                break;
            case 2:
                x = 0;
                y = random.nextInt(screenHeight);
                break;
            case 3:
                x = screenWidth - zombie.getWidth();
                y = random.nextInt(screenHeight);
                break;
            default:
                x = 0;
                y = 0;
        }

        zombie.setX(x);
        zombie.setY(y);
        zombie.setVisibility(View.VISIBLE);
        zombieActive = true;
        moveZombie(zombie);
    }
    private void moveZombie(final ImageView zombie) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(zombie.getVisibility() != View.VISIBLE) return;

                float gamerX = gamer.getX();
                float gamerY = gamer.getY();
                float zombieX = zombie.getX();
                float zombieY = zombie.getY();

                float deltaX = gamerX - zombieX;
                float deltaY = gamerY - zombieY;
                float distance = (float)Math.sqrt(deltaX*deltaX + deltaY*deltaY);

                if(distance > 0) {
                    float speed = 5f;
                    float newX = zombieX + (deltaX/distance)*speed;
                    float newY = zombieY + (deltaY/distance)*speed;

                    float prevX = zombie.getX();
                    float prevY = zombie.getY();

                    zombie.setX(newX);
                    zombie.setY(newY);

                    if(checkZombieCollisionWithBlocks(zombie)) {
                        zombie.setX(prevX);
                        zombie.setY(prevY);
                    }
                }

                updateZombieSprite(zombie, deltaX, deltaY);
                checkZombieCollisionWithGamer(zombie);

                moveZombie(zombie);
            }
        }, 100);
    }

    private void updateZombieSprite(ImageView zombie, float deltaX, float deltaY) {
        String direction;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);

        if(absDeltaX > absDeltaY) {
            if(deltaX > 0) {
                direction = "left";
                zombie.setScaleX(-1f);
            } else {
                direction = "left";
                zombie.setScaleX(1f);
            }
        } else {
            if(deltaY > 0) {
                direction = "down";
                zombie.setScaleX(1f);
            } else {
                direction = "up";
                zombie.setScaleX(1f);
            }
        }

        String spriteName = "zombie_" + direction + zombieStep + "_high";
        int resId = getResources().getIdentifier(spriteName, "drawable", getPackageName());
        if(resId != 0) zombie.setImageResource(resId);
    }

    private boolean checkZombieCollisionWithBlocks(ImageView zombie) {
        ViewGroup parent = (ViewGroup)zombie.getParent();
        int zombieWidth = zombie.getWidth();
        int zombieHeight = zombie.getHeight();
        float zombieX = zombie.getX();
        float zombieY = zombie.getY();

        for(int i=0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if(child.getVisibility() != View.VISIBLE) continue;
            String childName = getResources().getResourceEntryName(child.getId());
            if(childName.startsWith("BLOCK_")) {
                int blockWidth = child.getWidth();
                int blockHeight = child.getHeight();
                float blockX = child.getX();
                float blockY = child.getY();
                if(zombieX < blockX + blockWidth && zombieX + zombieWidth > blockX &&
                        zombieY < blockY + blockHeight && zombieY + zombieHeight > blockY) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkZombieCollisionWithGamer(ImageView zombie) {
        int gamerWidth = gamer.getWidth();
        int gamerHeight = gamer.getHeight();
        float gamerX = gamer.getX();
        float gamerY = gamer.getY();
        float zombieX = zombie.getX();
        float zombieY = zombie.getY();

        if (gamerX < zombieX + zombie.getWidth() && gamerX + gamerWidth > zombieX &&
                gamerY < zombieY + zombie.getHeight() && gamerY + gamerHeight > zombieY) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastZombieHitTime > 500) {
                ghp -= 10;
                lastZombieHitTime = currentTime;
                if (ghp <= 0) {
                    resetGameState();
                }
            }
        }
    }



    private boolean checkCollisionWithBlocks() {
        ImageView BLOCK_barrier = findViewById(R.id.BLOCK_barrier);
        ImageView BLOCK_barrier2 = findViewById(R.id.BLOCK_barrier2);
        ImageButton Attack = findViewById(R.id.Attack);
        int gamerWidth = gamer.getWidth();
        int gamerHeight = gamer.getHeight();
        float gamerX = gamer.getX();
        float gamerY = gamer.getY();
        ImageView city = findViewById(R.id.city);
        ImageView portal = findViewById(R.id.portal);

        ViewGroup parent = (ViewGroup) gamer.getParent();
        boolean collisionWithPortal = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            String childName = getResources().getResourceEntryName(child.getId());
            if (childName.startsWith("BLOCK_")) {
                int blockWidth = child.getWidth();
                int blockHeight = child.getHeight();
                float blockX = child.getX();
                float blockY = child.getY();
                if (gamerX < blockX + blockWidth && gamerX + gamerWidth > blockX &&
                        gamerY < blockY + blockHeight && gamerY + gamerHeight > blockY) {
                    return true;
                }
            }
            if (child == portal) {
                int portalWidth = portal.getWidth();
                int portalHeight = portal.getHeight();
                float portalX = portal.getX();
                float portalY = portal.getY();
                if (gamerX < portalX + portalWidth && gamerX + gamerWidth > portalX &&
                        gamerY < portalY + portalHeight && gamerY + gamerHeight > portalY) {
                    collisionWithPortal = true;
                }
            }
        }
        if (collisionWithPortal) {
            game = 1; // Игра активируется при входе в портал
            city.setVisibility(View.VISIBLE);
            for (int j = 0; j < parent.getChildCount(); j++) {
                View otherChild = parent.getChildAt(j);
                if (otherChild != city && otherChild != gamer && otherChild != joystickView && otherChild != portal) {
                    otherChild.setVisibility(View.GONE);
                }
            }
            BLOCK_barrier.setVisibility(View.VISIBLE);
            BLOCK_barrier2.setVisibility(View.VISIBLE);
            Attack.setVisibility(View.VISIBLE);

            handlePortalCollision();

            return true;
        }
        return false;
    }

    private void resetGameState() {
        // Сброс состояния здоровья игрока
        ghp = 100;
        personage = null;
        game = 0; // Игра становится неактивной при смерти персонажа

        // Скрыть зомби
        ImageView zombie = findViewById(R.id.zombie);
        zombie.setVisibility(View.GONE);
        zombieActive = false;

        // Показать лобби и выбор персонажа
        ImageView lobby = findViewById(R.id.lobby);
        ImageView city = findViewById(R.id.city);
        city.setVisibility(View.GONE);
        lobby.setVisibility(View.VISIBLE);

        // Скрыть игрока и элементы управления
        gamer.setVisibility(View.VISIBLE);
        joystickView.setVisibility(View.GONE);
        attackButton.setVisibility(View.GONE);

        // Показать персонажей для выбора
        ImageView boxerChar = findViewById(R.id.boxer);
        ImageView steampunkerChar = findViewById(R.id.steampunker);
        ImageView mushketeerChar = findViewById(R.id.mushketeer);
        ImageView karatelChar = findViewById(R.id.karatel);
        ImageView fone = findViewById(R.id.fone);
        ImageView start = findViewById(R.id.start);
        ImageView BLOCK_tree = findViewById(R.id.BLOCK_tree);

        ImageView BLOCK_freezer1 = findViewById(R.id.BLOCK_freezer1);
        ImageView BLOCK_freezer2 = findViewById(R.id.BLOCK_freezer2);
        ImageView BLOCK_taburet1 = findViewById(R.id.BLOCK_taburet1);
        ImageView BLOCK_taburet2 = findViewById(R.id.BLOCK_taburet2);
        ImageView BLOCK_truba = findViewById(R.id.BLOCK_truba);
        ImageView BLOCK_boxing = findViewById(R.id.BLOCK_boxing);
        ImageView BLOCK_redthing = findViewById(R.id.BLOCK_redthing);
        gamer = findViewById(R.id.gamer);


        ImageView portal = findViewById(R.id.portal);
        ImageView BLOCK_barrier = findViewById(R.id.BLOCK_barrier);
        ImageView BLOCK_barrier2 = findViewById(R.id.BLOCK_barrier2);

        joystickView = findViewById(R.id.joystick_view);
        joystickView.setVisibility(View.GONE);
        attackButton = findViewById(R.id.Attack);
        attackButton.setVisibility(View.GONE);

        boxerChar.setVisibility(View.VISIBLE);
        steampunkerChar.setVisibility(View.VISIBLE);
        mushketeerChar.setVisibility(View.VISIBLE);
        karatelChar.setVisibility(View.VISIBLE);
        start.setVisibility(View.GONE);
        fone.setVisibility(View.GONE);
        lobby.setVisibility(View.VISIBLE);
        BLOCK_tree.setVisibility(View.VISIBLE);
        BLOCK_boxing.setVisibility(View.VISIBLE);
        BLOCK_freezer1.setVisibility(View.VISIBLE);
        BLOCK_freezer2.setVisibility(View.VISIBLE);
        BLOCK_redthing.setVisibility(View.VISIBLE);
        BLOCK_truba.setVisibility(View.VISIBLE);
        BLOCK_taburet1.setVisibility(View.VISIBLE);
        BLOCK_taburet2.setVisibility(View.VISIBLE);
        karatelChar.setVisibility(View.VISIBLE);
        boxerChar.setVisibility(View.VISIBLE);
        steampunkerChar.setVisibility(View.VISIBLE);
        mushketeerChar.setVisibility(View.VISIBLE);
        portal.setVisibility(View.VISIBLE);
    }

    private void handlePortalCollision() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("Действие после столкновения с порталом");
                spawnZombie();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }
}
