package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.tools.SampleDataGenerator;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;

import javax.persistence.Column;


public class GenerateDataAction extends SampleDataGenerator implements StallionRunAction<CommandOptionsBase> {
    @Override
    public String getActionName() {
        return "generate-data";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {
        AppContextLoader.loadCompletely(options);
    }

    @Override
    public void execute(CommandOptionsBase options) throws Exception {
        generate();
    }


    @Override
    public Long getBaseId() {
        return 10000L;
    }

    @Override
    public void generate() {
        createUsers();
        createChannels();
        createMessages();
    }

    private long GEORGE_ID = 10000;
    private long PAUL_ID = 10001;
    private long JOHN_ID = 10002;


    public void createUsers() {
        List<Combo> combos = list(
                new Combo()
                        .setProfile(
                                PROFILE_GEORGE
                        ).setUser(
                            new User()
                                    .setEmail("georgewashington@stallion.io")
                                    .setGivenName("George")
                                    .setEncryptionSecret("41XMF2YvTWvs")
                                    .setFamilyName("Washington")
                                    .setId(GEORGE_ID)

                        )
                ,
                new Combo()
                        .setProfile(
                                PROFILE_PAUL
                        ).setUser(
                            new User()
                                    .setEmail("paulrevere@stallion.io")
                                    .setEncryptionSecret("iO5D9pXLms0k")
                                    .setGivenName("Paul")
                                    .setFamilyName("Revere")
                                    .setId(PAUL_ID)
                ),
                new Combo()
                        .setProfile(
                                PROFILE_JOHN
                        ).setUser(
                           new User()
                                   .setEncryptionSecret("iO5D9pXLms0k")
                                   .setEmail("johnadams@stallion.io")
                                   .setGivenName("John")
                                   .setFamilyName("Adams")
                                   .setId(JOHN_ID)
                )
        );
        for(Combo c: combos) {
            User user = c.getUser();
            //user.setEmail(user.getEmail());
            user.setApproved(true);
            user.setEmailVerified(true);
            user.setUsername(user.getEmail());
            user.setRole(Role.MEMBER);

            user.setBcryptedPassword("$2a$10$V2B71X0xH/tS0udKoBeIzelx6AjTlVwxn9.0n/bULXSFdhL5ATo7K");
            user.setEncryptionSecret(GeneralUtils.md5Hash(user.getEmail()));

            UserController.instance().save(c.getUser());
            c.getProfile().setUserId(user.getId());
            c.getProfile().setId(user.getId() + 200);
            UserProfileController.instance().save(c.getProfile());
        }
    }

    public void createChannels() {
        List<Channel> channels = list();

        channels.add(
                new Channel()
                        .setName("General")
                        .setChannelType(ChannelType.CHANNEL)
                        .setId(newId(500))
        );
        channels.add(
                new Channel()
                        .setName("Humor")
                        .setChannelType(ChannelType.CHANNEL)
                        .setId(newId(501))
        );
        channels.add(
                new Channel()
                        .setName("Officers")
                        .setChannelType(ChannelType.CHANNEL)
                        .setHidden(true)
                        .setInviteOnly(true)
                        .setId(newId(502))
        );
        channels.add(
                new Channel()
                        .setName("Long Discussions")
                        .setChannelType(ChannelType.FORUM)
                        .setId(newId(503))
        );

        channels.add(
                new Channel()
                        .setName("George and Paul")
                        .setChannelType(ChannelType.DIRECT_MESSAGE)
                        .setId(newId(504))
        );

        for(Channel channel: channels) {
            ChannelController.instance().save(channel);
        }

        Map<Long, int[]> membership = map(
                val(GEORGE_ID, new int[]{500, 501, 502, 503, 504}),
                val(JOHN_ID, new int[]{500, 501, 502, 503, 504}),
                val(PAUL_ID, new int[]{500, 501, 502, 503, 504})
        );
        for(Map.Entry<Long, int[]> entry: membership.entrySet()) {
            for (int channelIdBase: entry.getValue()) {
                ChannelMember cm = new ChannelMember()
                        .setUserId(entry.getKey())
                        .setJoinedAt(ZonedDateTime.of(2011, 1, 1, 12, 0, 0, 0, UTC).plusDays(channelIdBase))
                        .setChannelId(getId(channelIdBase))
                        .setOwner(entry.getKey().equals(GEORGE_ID))
                        .setId(entry.getKey() + 100 + channelIdBase);
                ChannelMemberController.instance().save(cm);
            }
        }

    }

    public void createMessages() {
        Integer base = 800;
        int x = 0;
        int i = 0;
        String[] froms = {"paulrevere", "georgewashington", "johnadams"};
        Long[] fromIds = {PAUL_ID, GEORGE_ID, JOHN_ID};
        for(;base<820;base++) {
            String body = lines[i];
            Map data = map(val("bodyMarkdown", body));

            Message message = new Message()
                    .setFromUsername(froms[base % 3])
                    .setFromUserId(fromIds[base % 3])
                    .setChannelId(10500L)
                    .setCreatedAt(ZonedDateTime.of(2016, 5, 1, 12, 0, 0, 0, UTC).plusHours(base))
                    .setMessageJson(JSON.stringify(data))
                    .setId(newId(base));
            MessageController.instance().save(message);
            i++;
            for (Long userId: list(GEORGE_ID, JOHN_ID, PAUL_ID)) {
                x++;
                UserMessage um = new UserMessage()
                        .setHereMentioned(false)
                        .setMentioned(false)
                        .setRead(false)
                        .setUserId(userId)
                        .setMessageId(message.getId())
                        .setDate(message.getCreatedAt())
                        .setId(newId(2000 + x));
                if (base >= 818) {
                    um.setRead(false);
                }
                UserMessageController.instance().save(um);
            }
        }

    }

    String[] lines = new String[] {
            "Hare ahead under recast invidious versus more redoubtably crud like cordial the gorilla tore broke trenchant characteristically that much this before.", "Goodness owl this even moaned and horse black ireful salamander abidingly less reindeer poutingly cheered jeez this dachshund in imaginative touched much had fought lucky one hey.", "Then and ground panther badly cozy brave unheedfully much lynx some alas and until grinned hen various out misheard robustly annoying this far in yawned barring smelled one.", "That completely leered swept darn concomitant portentous less trim much more oyster one so much grizzly regardless therefore avowed much eagle saliently and chose far the directed juggled through dear far did some.", "Jeering vulture more stung hello sheep up far dizzily and smoked this one sardonically manatee so some among jeez ahead panther goodness the oh punitively expeditious much crud arousing ouch over far.", "Dear hardheaded incongruously much one after sporadic out dear ground some some far goodness astride with suspicious specially where oh conjoint more and yikes and well opposite persistently.", "Ouch below that a goodness emptied flung this amidst wildebeest extraordinary contrary set after overpaid llama when underneath wrongly more one.", "Kiwi far dalmatian dear this bastard turbulent much much ouch brusquely a due broad far less camel a contumacious woodchuck continual that retrospectively this lethargic told healthy conjoint gosh impalpable thus.", "Buffalo dear goldfish and winced far a ardent noble and one more because because pinched one coasted gull porpoise hey rakish one climbed honey and some among.", "That caught tapir but jeez tamarin whispered much far conscientiously far darn after crud adequate sparing despicably thus lemming lion crud strove more.", "That globefish jeez where shoddily said far one up hey thick a this that alas one abstrusely exorbitantly gazed imaginative cow more unblushing fiendish as goodness wow much turgidly scurrilously far climbed knelt statically.", "Since beneath some excepting less before over spontaneously grabbed bandicoot blithely handsome that caustically wrongly took ambidextrous beat and past dear less fell owl sniffed gosh much.", "Conjointly sane manful egret piteously undid snapped alas one terrier rode labrador the more manatee ouch emu less jeez in bald more inconspicuously hardy gosh underneath because hawk far.", "At lemming and boastfully boastfully hey dear so one the ravingly badger ethereally outside much more where above about alas flamingo by alas close some free disbanded.", "About melodious commendable dropped purely out hilarious tore ouch beneath truthfully far warthog crud consoled ravenous as timid creepily idiotic a that goodness cringed goodness outside tapir much more goodness.", "Hello crud above robust panther a wherever beside much ecstatic and some gorilla hey testy less terse darn more casually jeez rattlesnake jeepers the domestic much amongst that some due.", "Sociable simple in piranha hatchet excursive far that one religiously a however overdrew well yikes kangaroo stiff busily peculiar goose tautly successful.", "Far placed alas oh more and ecstatically on desolate orca crud beside goodness far aside one much and overate darn wombat artificially waved tellingly reined yet ruggedly a.", "Jeepers and forgetfully heard deliberately ruthlessly jeez peered less vigorously more less froze fumed before froze some excellent macaw and one the outbid ouch the soggy far the accidentally dear emoted however.", "Much jeez wishful far alas extraordinarily where musically more oyster due far jeez ouch overrode ungraceful wallaby below flailed truthful crucially outside much spoke cast triumphantly creatively weird."
    };



    public class Combo {
        private User user;
        private UserProfile profile;


        public User getUser() {
            return user;
        }

        public Combo setUser(User user) {
            this.user = user;
            return this;
        }


        public UserProfile getProfile() {
            return profile;
        }

        public Combo setProfile(UserProfile profile) {
            this.profile = profile;
            return this;
        }
    }

    UserProfile PROFILE_JOHN = new UserProfile()
            .setEncryptedPrivateKeyInitializationVectorHex("f36de0c0f5f7c8dd03d4f17a598cac48")
            .setPublicKeyHex("30820122300d06092a864886f70d01010105000382010f003082010a0282010100ab33b4e5a25937d2b47738e7abca2a1a7bb36f82a8226ec511732bc4b78e5038c2365594e305b064def8a5218ac7e71e3169b9ef503c19545752bb337c19a6df396a80194b1c7ba8d66b4942bcb53a0367d317edc0eb3963e11da5ff3e7440ed3124a31396d563d4ed2517e4a41e18a88925521613ae5b8100ddf125dfb94c227b16b940c58a57add6125646772ca928f1738b017146fe97f5174c758611577fd5775dcea54e249979cf29d9e7c1161ef1e42352a2f5c43bc751b773f71d4e229f795274dcc4bb357c04f4ab1b1a7184c325aa100a0936b4c100dca7f7b88b60d58dd53abdaa3952f9a744315ff12b581319d7e6e7a0e6c05e3497fdf87b73510203010001")
            .setEncryptedPrivateKeyHex("399306517ac3c0b8bca440fe1e7bcb2b0b8a294405328f885a4acb853d8830e3e38970d08e5cee8834bb323fff3951b9f76f312e3596cd66d2fc52ed9a8bf04c60c84eeb223abf9c5ccbf950cb7371523bd64e0d7ac7d1d2c8ebaf3626cbc263a11ef7f05eb6f0dbf645fbfd9a98425e673b7dbf3d52ab7283e894ad656806c3538cc1152238a7701135431002b4be76c982d87b1120c918169c150dd63c131f5a34948fe772cc57daa96963bcdf49766ac03f8424aedbfa6576ef05cd2aac9d6a33f34a758d410e23c1c00fb398ae03207ef3e84b8bae13ccf1ae58c861f1b4527eabed815b590e202d955efa7548dcf22574e0b4ce9db79ad035fb41912e3859a256cc25ab0ad264f900a5f98b893b15c0017e6e105fd2ab89152f9507a0949d7bc45d27850999361146a3249aaadce31b44d4f5d8d0b8f8d9b0c41f62e08940680869313636050be6bccbe0ffb5b1f5a83770cc54290f8f914bb1eba1bf3bac652fab5b89a99c7655c7fc198086cb2de3cf2b676f7c64646096350dea1b0ba06094907bee537b6227dca43f4084a59e0e05b532b705832cad0480cf5e24a872665fa806bd47a133830b771d704d20b36385052f88c32cc26767b42e049ee4bcd4272b129851e87ef0a141ad2def958cb2409ab5feb0de21aad0eea5c17416d5421991a20771ef5275db4c92ef8ea5c8e3a2d7e76ecbb05407db2c6f92b03cb977f0bb94632273d802b63664900d81ccdd1bcc18bfea938613d9c0cb5c914f80fc10898a44fd20404e58c418f3c9865752c1a21fb315e17bfd6a2f1a27e428c6a064caf8faa1f49d359dca4a93138c807544cc265ee852046c0f0c7c9e7d0d879ee186fc81233f3efb6f53a4c77889982325e964d48d35cc598891c9ec1539c44f8b1a2236f4fd340269221211b0de7ec98c557b8926866c5ece6cf68e93a92d18c7cc148858fc9f3caa6915a6cb67f1f42bddfdf4ba476778237a252d57489012927c23b75be9ef60ce0303de66ece36e62925e45b966042d7715631243928e3b8b78c3a54d66b2c743d8d3ed39a20a2aa7fbcf03e3a4b54a202b732cea1d0dbfc92a7b66b5ba21af9d11c8849d031a0f5e34d9096325f2e33e32c2491a9c1a515c50f4dbba2b93362cd159810bb2db1457322743afe13bc4ae0323385daaa0306980fc27b35bbad4052c7da92b2412c68565a735de255af38fa9735d62ad6f02750e0432dfa00a789e9a1b4271b1050bd0861c2c3823e541a51497535c137a508928260efb787b076639ec0821494218b6d8077763d20ef330fe0a3e2fe114e4fbb19e71d79055534ef896e515bb02e6e2eb9aab839346a670efeceeb13c18359f9375894c6c578352838dc89e5e0f99e2a9450aa8a03baf7c26da00ea4904584be21b595f0632b8b7830ba4b9245b0f33074328f54b8d1b76fbcb52ab9a52d0c1e175cbcc25e5404dc80cb810f14374134cbe9e0b306a1aa1c71415c898fb5d83279b7f4a492104f6be8f6040758ef5001a425b5dabe4bb3c6bfc45259841f4bac3fd1f3d20f8db2929cb493b349a58026abc3fcbd1597840b7186d4ac4fce983c9dd0b9744c2b57ac78121ca3a784381fae18cf1604b7f1454db29dfb6004bff9f04c7b1eb580abc02ca2f861d43ab43d5e2628b74cb59c4f53fcb6b434bcfa5a3c4c5f335d857b575c67ab6a20257629bae0fdd7374ff647b8bca116eb9a9df5d045bcc9efe8d917dfea6c65c")
            ;

    UserProfile PROFILE_GEORGE = new UserProfile()
            .setEncryptedPrivateKeyInitializationVectorHex("31a6c6a7f67db2529e9aca9fad69b30d")
            .setPublicKeyHex("30820122300d06092a864886f70d01010105000382010f003082010a0282010100b97f750247ba25fee36a9c62102e7ae1ec8c371c47a09022b137be6341d0b278083c221fcbc73c47aa12305c9b85b3f6667e147c987ae9f820c691015b217c60320f0d4c041b9295a7f1df7b50f8455e7837a67b2657eabfdcfb51c1e6f98cbdbd37c0040da528af9da145f44961c1c3ada4fef4df6163d485b1b3a06b347da41a7ac2d2749f8abffdf05adbcb24b1c0ce1c426dd7971734010b7113d5d98bc71981e2c43ec947d06f393d098f48a4969f303ef06cd316ac8a581fa22b6b1fe6d167a2c7e5e256ed445019610a6665ad778d15ebf97aca90892f64228a4457d85454cc64aa7967bb110b2d16cb12ae63bfb531da3c9c16245e61b627741482570203010001")
            .setEncryptedPrivateKeyHex("20edc62398a89d898f8825c7185fe41aa1621f2b3609590cf34e3bf73414e7a650e0291ccdaab8f4a75f008d042e89abaea4c3bd9256de4adb0812fbf86037832e83f48c3b87a48da318ce814253c24466156c180474536ef1fc9772129c3a8f0086c30f505891d00b096820dfd4830e646c7c0e0464ab921bfa79c558e7ebca0c84195442dee2292a9bdb4c78fbc264b3e4079c724c15e64ed4f484055587305f5462277db012b504f7bd00758a37a67bf9ce47ba06eba0f9643d503f72684e7fab7b4e689b55c1be76af116355553db780ca5af41adb54978439d9947853efb561836e56e0327100e2487c39cccd63fe6c6c59704fa9f09bef7ec54a7dcf76f3169a81202d70ca0a83040dea7d0b0b17a453147e2d841f9463c8fc2eea9be4ca30c15622b4089d3bb3c048e50e1bdd1420a558088f611fe7851262ac9b90167b8b7a4707ea3d9c0e7db9bf880e0b3c97bf12eb2330d4e3c3bd227ec08b21069a28dc39bad1cff1d63ed51eed0a7246580c3a104afa94b2d45647a83e1cd635ab59f5ad5d5ee0c64f6e36b8aec5c40bcf2f307dfddfad26bf92d00b9c9412bef1b9707fe5ae1f2371fe0484bfc6568e2ce4738d1060a613bc6430c8cd97d1fa59d7c51d559382b14d87c692b75797fe6174816a66cf67c8cbf13200e6175d3aaf88c76ed2603b7c5cff429860ce8e928aef0dd66684c34d6dd7386cd0b1ed980585f1366d6869c35535f24eff7eb9cd7e95481b8a58df564be3d2e284ecc2d7b5add6c2badb36a39bd40905c948620413421d4258a6901fe5c132f38da682f1b633cc7e90e8a61275c81f821a247e0c866af748da79ac52d19ec65e6426e841f1c6d8b9c459ae2c65ac62ce2c254d7db3b01db31f9216b8df54a4f46b6bf01aa31db11a078a942a504dbdcefd6af61ff401c7a2dc9508d7822639d43d12dbca6cdd4af3f5621a25410087a30ca0cea001cc90af03d482b50cf3aa762f87226e891aeac5b809391cdb5d8370cad2b286e7596749386518c1f2723c40dd68b44859367a15591ec0e6975b609636a8e5cce402328247abf812815f2f057e86e29fc80436fa2926484271ebcb47b4322cee39861dfb8ac86184d10fd496792cac48055c983361319d7735cc33e97571da61906d77adfafb20e36466b3d8798d816247559d24548003eb8dce5c39b18e45c9a537d09c207898bbc582d9efd2897617ccf72ec26c3bc6bc39b94e739b0ca5b8a0102916c3190a3e7f63fbb111e98ad804f577e3778cc1aff58fa0ca56fc4387ada277ee936ed08370c47f7ec043a9105b6038a9a0ab429a2ec3d6e28265a5b60b34146aefc68634a515b7b8592cfbe6ca3a625e52b680f76ebbb7e9f6821fba223cdb08d52af77bf1a75046562c0d87e84daf69031a390a2681d5872560a8c3d341fd094f752079899eae9ae9a8a603c0a3e25e55fc8a738a0165ecc424bc9c420c3859d954c1d066483e876b21a7e699fe0dcce9d822c032dbd1aa3d52f0a9bb5b6d094188c308495dda537eb3c938a284509ac2aa7694cc053f14fd0941508dacb941ece3bdde1ae11b9bac463f333a2bd886d8315cfcdbb2c31bf4214fc5a0ac2c322e328b8b52247a712956f4f1636c8e7a95f095c1af4bf1d3910a838c222d8dd756dc1872e30f57ff2ebe7665e428ac2752a0f37eb88a1b6523cbcee2751203795123f4e5b4b744b84baa9b9db58863c1b60f5d1b6735d40bf3f9a219")

            ;

    UserProfile PROFILE_PAUL = new UserProfile()
            .setEncryptedPrivateKeyInitializationVectorHex("89801187a0ebf05949fecd83426c1a78")
            .setPublicKeyHex("30820122300d06092a864886f70d01010105000382010f003082010a0282010100b67406c4dfa2d504d38dc5e342d495015849235bffb0430f65c2396c0980463745514edc1718d75146c5071feda85cfd763a8cefa78ffcf0a806412f8d9fa9c9761bf3daebbe828a2a6cf07a894d2eab0db620102473bb54571ba2fdfd88c62223dee0c9a70a47e1becb8ffd68eacb7879c06d579d5540b0013704a0a6a7bf6c2f078ea6ee80f6dd923a4bdcbb613367f8f186d6420e39b457b525e1c24665a39a2e2ebd838a750fe131440b3921631cd930c5c7afde98176d62b2094ea540d3e7e2967fc05eed253e53951a65c8cab0f425cf8d55f5743151f36034ded04adf1bcc2bd5834fb8e3cce395f3d9c144e9b2583dbb772a2dbd27e7597bc4ba51db0203010001")
            .setEncryptedPrivateKeyHex("92376185da9cf2b5fd07e184fa4ad9d9ef955801b48cd859515d63176ff52e7cd9da4c22554911bf0960c051cf70f63f8bdfeb894715d1a1ee498da26516293951a802fd6f79345e4aab9a6b47a8b7690bf24d76dda81eb6e0e1fcc279a617fa668d491c272ff140c61c20d4bd04912ba1ced82e2bdb81ae5427f17fc6587dcbb6cf062fbd147ebbe70068943e0527f64eaaf9cfa2e2bf1767964746a3eaeb186cf5356b7fe2e3a882aa9fdb648b66634e5e9c153eb76831c6c078b98f6d19829ba90ab3bb96d0445b8f4ca24231328de8c0d454bf44694a49f0ff335b97ff252d7d1a1a67fdb4081e07c4aeae6c57eb7cd0788104f28ac25224c382d81c8bd4b85591ba82a1d17549d187c730074fb770171c7ee2d6dd6f6fe9ad4abb65170346264c04593a49baaea1213fa340db2dd4c2776e2a09a0398fc2d2e298b03866231000ea881f30ad01af84f5fe8751554c135062c94d24aba82af9247acd272bb7215060b30d916e89142ceecf5e31dbfd28a49b3eb0191bae7ad7137942d316e7f456e2ecb4faccd23d22974c60879662d1fc10a0f7d538fdf25fbe3daf168de52594833b80767fbfe65bcf8a3357eeb48231169c748229de31874aed54e0cfa1f40eece0646d7252e024db98b7debf2d0d74a94eaf9ea395c9aaf467682ec775e0cd26196da51037916626f15bb496631cc7bc7031d704b730819db30f2e6876c7334421a80c02db0119a1f7aa3f7b060bf7bd9ddefe3bbfd3f3fe63f10cc2f6aee4d0f002ac18dedd5818216a38968405127242594628c25fb6bf0f75bdde2220997d2ee5f1d1c78b4fc968a481b3db5bffc50721e0a7d84689f5d00bf21d91dbc025c8add23883c420ce390fe2ac4541f581da200d02dff9441716e520c25a59705fea0c493f1e84c5fad1783c4929f8df0672eb58f8407e5b88a42246a7eadfd371ce5e2ca33d66fff287ced3d231da6f5318c6468abf02f9be1761f0111c67ba10efa87b7aa499b06129136f45eaabb7e1e888c6788d5dab71ff495812953f8e844990e5767476c690eb6c040e2580f8c36a23c7313d4bd1c3462e83fe6e4bae6716ee08863186cee919e400ea951d190a40beecb04a40c37d3605ae7e30b4fd7ed460a1217ec415220d27652be0552f4190d8f68170edf7af49d267baa6607312d7abe5bf99eaf31ad6b68348d186459e1c1273dec7da14bde836c21b3bdf37394f977fdaa1a873065d22bf86e60b2676f212790a7e333fcfddd6cbfd145c382f7a91d595a7030f360d52d260d64d37617c83c8d3964759d8b2e9acccfc3d144495a0ba308b8f0b7f1ac432c90e6d7387500134304f30072d9ba10942f6b5a67d7004e0093b02dcb6fb537a709413eac246b1e76933aeea0d54ad65c81678c42509c514d1f29349842f7fe01cf95df5c2d2a1417dba6a9478032f3425ce21c63d557e4d928cf5a6e9041388d818552c6c4a049c3fe60de7588d365b2861b35f3407fb06a03c4e2c4fa37f615ea73ee86f665d86dfd3ce421a7bed5242877f44e0c591498deb25b0d35b2aef7451895fc77e0f95317fc3011bddc4b2c656d82227ce3ac8bdcd2867fe3c187e1b3f4a35c0a39f23d4678676f2ce4b87b3e4bedbb169670612edb5017c9db0428f52d33bb54a1fab1dfc299b6d3e0ec8520f76653fe82d4b9b7072cc51ec4f30da879788fa5b3a9b9e94df2d53939191bf58034404eee618a51b6499fa86d19a9a")
            ;



}
