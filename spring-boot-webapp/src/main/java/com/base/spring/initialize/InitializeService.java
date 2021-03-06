package com.base.spring.initialize;

import com.base.spring.domain.*;
import com.base.spring.repository.GroupRepository;
import com.base.spring.repository.RoleRepository;
import com.base.spring.repository.TreeRepository;
import com.base.spring.repository.UserRepository;
import com.base.spring.utils.BCryptPassWordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Description : TODO(初始化相关数据)
 * User: h819
 * Date: 2016/1/18
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class InitializeService {
    //private static final log log = LoggerFactory.getLogger(InitializeService.class);

    @Autowired
    TreeRepository treeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    RoleRepository roleRepository;

//    private boolean initMenuTree =false;
//    private boolean initUser =false;
//    private boolean initGroup =false;
//    private boolean initRole =false;

    /**
     * 初始化 TreeNodeType 中定义的所有类型的根节点，所有节点均为该节点的子节点
     *
     * @PostConstruct // 在 Bean 初始化之前执行的操作
     * @PreDestroy //在 bean 销毁之前执行的操作
     */
    @PostConstruct // 在 InitializeService Bean 初始化之后执行的方法
    @Transactional(readOnly = false)
    public void initBaseData() {

        /**
         * 初始化 Tree ，保证根节点存在
         */

        List<TreeType> rootTreeType = treeRepository.findTreeTypes();
        List<TreeType> listType = Arrays.asList(TreeType.values());
        for (TreeType type : listType) {
            if (!rootTreeType.contains(type)) {
                log.info("init {} tree root node", type);
                TreeEntity root = new TreeEntity(type, "root_" + type, 0, true, null);
                treeRepository.save(root);
            }
        }

        /**
         * 初始化 Tree
         */
        initMenuTree();
        initPageResourceTree();
        initStandardTree();

        /**
         * 初始化 User
         */

        initUser();
    }

    /**
     * 初始化菜单树，作为基础树
     * 其中下面几个树节点，不能修改，为本系统默认的菜单(index.ftl 导航也是这个名称)
     * 1. 菜单管理
     * 1.1 树结构管理
     * 1.1.1 菜单树
     * 1.1.2 部门树
     * 1.1.3 其他业务树
     * -
     * 1.2 用户管理
     * 1.2.1 用户
     * 1.2.2 用户组
     * -
     * 1.3 角色管理
     * 1.3.1 菜单角色
     * 1.3.2 标准资源角色
     * -
     * <p>
     * 其他菜单只作为示例，可根据实际需要修改
     */
    private void initMenuTree() {

        /**
         * 包含 "菜单管理"，意味着已经初始化，
         */
        List<TreeEntity> exist = treeRepository.findByName("菜单管理");

        if (!exist.isEmpty()) {
            log.info("menu tree has initialized , continue ");
            return;
        }

        log.info("initialize menu tree ... ");
        createMenuTree();

    }

    /**
     * 1. 按钮/资源
     * 1.1 菜单树
     * 1.1.1 树结构编辑
     * 1.1.2 节点 URL
     * -
     */
    private void initPageResourceTree() {

        /**
         * 包含 "按钮/资源" ，意味着已经初始化，
         */
        List<TreeEntity> exist = treeRepository.findByName("按钮/资源");

        if (!exist.isEmpty()) {
            log.info("menu tree has initialized , continue ");
            return;
        }

        log.info("initialize pageResource tree ... ");
        createPageResourceTree();

    }

    private void initStandardTree() {

        /**
         * 包含 "按钮/资源" ，意味着已经初始化，
         */
        List<TreeEntity> exist = treeRepository.findByName("国家标准");

        if (!exist.isEmpty()) {
            log.info("menu tree has initialized , continue ");
            return;
        }

        log.info("initialize standard tree ... ");
        createStandardTree();

    }

    private void createPageResourceTree() {
        //系统启动的时候，这几种类型的父节点已经创建完毕，在下面添加子节点即可
        TreeEntity pageResource = treeRepository.findRoot(TreeType.PageResource).get(); // PageResource
        Set<TreeEntity> set = new HashSet<>();

        /**
         * url 只适用于 ace admin 1.4 , ajax 方式
         * 注意 url 应该和 index.ftl , NavigateController 中 RequestMapping 对应
         * #page/admin/ztree-type?treeType=Menu
         * 只填写 #page/ 后面部分
         */

        /**
         * 初始化 "按钮/资源"，为系统默认菜单，不能改动
         */
        //一级菜单
        TreeEntity menu = new TreeEntity(TreeType.Menu, "按钮/资源", 0, true, pageResource);
        set.addAll(Arrays.asList(menu));

        /**
         * 初始化 "按钮/资源" , 可以根据业务系统需要自行设计，这里仅为演示
         * -
         * 按钮/资源菜单仅为授权使用，系统不显示
         *
         */
        //一级菜单
        TreeEntity menu1 = new TreeEntity(TreeType.Menu, "菜单树", 0, true, menu);  //应用中的文件资源
        TreeEntity menu2 = new TreeEntity(TreeType.Menu, "文件资源", 1, true, menu);
        TreeEntity menu3 = new TreeEntity(TreeType.Menu, "其他资源", 2, true, menu);
        TreeEntity menu4 = new TreeEntity(TreeType.Menu, "PDF 文件", 3, true, menu);  //单独对 pdf 的操作
        set.addAll(Arrays.asList(menu1, menu2, menu3, menu4));


        //菜单树
        TreeEntity menu11 = new TreeEntity(TreeType.Menu, "树结构编辑", 0, false, menu1);
        TreeEntity menu12 = new TreeEntity(TreeType.Menu, "节点 URL", 0, false, menu1);
        TreeEntity menu13 = new TreeEntity(TreeType.Menu, "节点关联标准", 0, false, menu1);
        set.addAll(Arrays.asList(menu11, menu12, menu13));

        //文件资源
        TreeEntity menu21 = new TreeEntity(TreeType.Menu, "查看文件", 0, false, menu2);
        TreeEntity menu22 = new TreeEntity(TreeType.Menu, "下载附件", 0, false, menu2);
        TreeEntity menu23 = new TreeEntity(TreeType.Menu, "查看列表", 0, false, menu2);
        set.addAll(Arrays.asList(menu21, menu22, menu23));

        //其他资源
        TreeEntity menu31 = new TreeEntity(TreeType.Menu, "显示图片", 0, false, menu3);
        TreeEntity menu32 = new TreeEntity(TreeType.Menu, "资源列表", 0, false, menu3);
        set.addAll(Arrays.asList(menu31, menu32));

        //PDF 的操作
        TreeEntity menu41 = new TreeEntity(TreeType.Menu, "查看", 0, false, menu4);
        TreeEntity menu42 = new TreeEntity(TreeType.Menu, "打印", 1, false, menu4);
        TreeEntity menu43 = new TreeEntity(TreeType.Menu, "下载", 2, false, menu4);
        set.addAll(Arrays.asList(menu41, menu42, menu43));

        treeRepository.saveAll(set);

    }


    private void createStandardTree() {

        //系统启动的时候，这几种类型的父节点已经创建完毕，在下面添加子节点即可
        TreeEntity standard = treeRepository.findRoot(TreeType.Standard).get();
        Set<TreeEntity> set = new HashSet<>();

        /**
         * 初始化 Standard
         */
        // standard 1
        TreeEntity standard1 = new TreeEntity(TreeType.Standard, "国内", 0, true, standard);
        set.addAll(Arrays.asList(standard1));
        TreeEntity standard11 = new TreeEntity(TreeType.Standard, "国家标准", 0, true, standard1);
        TreeEntity standard12 = new TreeEntity(TreeType.Standard, "行业标准", 1, true, standard1);
        TreeEntity standard13 = new TreeEntity(TreeType.Standard, "地方标准", 2, true, standard1);
        set.addAll(Arrays.asList(standard11, standard12, standard13));

        // standard 11
        TreeEntity standard111 = new TreeEntity(TreeType.Standard, "国家标准", 0, false, standard11);
        TreeEntity standard112 = new TreeEntity(TreeType.Standard, "军用标准", 1, false, standard11);
        TreeEntity standard113 = new TreeEntity(TreeType.Standard, "计量规程", 2, false, standard11);
        TreeEntity standard114 = new TreeEntity(TreeType.Standard, "计量规范", 3, false, standard11);
        set.addAll(Arrays.asList(standard111, standard112, standard113, standard114));

        // standard 12
        TreeEntity standard121 = new TreeEntity(TreeType.Standard, "安全生产", 0, false, standard12);
        TreeEntity standard122 = new TreeEntity(TreeType.Standard, "电子信息", 1, false, standard12);
        TreeEntity standard123 = new TreeEntity(TreeType.Standard, "医药健康", 2, false, standard12);
        set.addAll(Arrays.asList(standard121, standard122, standard123));

        // standard 13
        TreeEntity standard131 = new TreeEntity(TreeType.Standard, "北京DB11", 0, false, standard13);
        TreeEntity standard132 = new TreeEntity(TreeType.Standard, "天津DB12", 1, false, standard13);
        TreeEntity standard133 = new TreeEntity(TreeType.Standard, "河北DB13", 2, false, standard13);
        set.addAll(Arrays.asList(standard131, standard132, standard133));

        // standard 2
        TreeEntity standard2 = new TreeEntity(TreeType.Standard, "国外", 0, true, standard);
        set.addAll(Arrays.asList(standard2));
        TreeEntity standard21 = new TreeEntity(TreeType.Standard, "iso标准", 0, false, standard2);
        TreeEntity standard22 = new TreeEntity(TreeType.Standard, "iec标准", 1, false, standard2);
        TreeEntity standard23 = new TreeEntity(TreeType.Standard, "itu标准", 2, false, standard2);
        set.addAll(Arrays.asList(standard21, standard22, standard23));

        treeRepository.saveAll(set);
    }

    private void createMenuTree() {

        //系统启动的时候，这几种类型的父节点已经创建完毕，在下面添加子节点即可
        TreeEntity menu = treeRepository.findRoot(TreeType.Menu).get(); // menu
        Set<TreeEntity> set = new HashSet<>();

        /**
         * url 只适用于 ace admin 1.4 , ajax 方式
         * 注意 url 应该和 index.ftl , NavigateController 中 RequestMapping 对应
         * #page/admin/ztree-type?treeType=Menu
         * 只填写 #page/ 后面部分
         */

        /**
         * 初始化 "菜单管理"，为系统默认菜单，不能改动
         */
        //一级菜单
        TreeEntity menu1 = new TreeEntity(TreeType.Menu, "系统管理", 0, true, menu);
        menu1.setCss("menu-icon fa fa-cogs red bigger-130");
        menu1.setUrl("#");
        set.addAll(Arrays.asList(menu1));
        /**
         * 系统默认菜单，不能改动
         */
        TreeEntity menu11 = new TreeEntity(TreeType.Menu, "菜单管理", 0, true, menu1);
        menu11.setCss("menu-icon fa fa-sitemap");
        menu11.setUrl("#");
        TreeEntity menu12 = new TreeEntity(TreeType.Menu, "用户管理", 1, true, menu1);
        menu12.setCss("menu-icon fa fa-user-plus");
        menu12.setUrl("#");
        TreeEntity menu13 = new TreeEntity(TreeType.Menu, "角色管理", 2, true, menu1);
        menu13.setCss("menu-icon fa fa-object-group");
        menu13.setUrl("#");
        TreeEntity menu14 = new TreeEntity(TreeType.Menu, "系统设置", 3, true, menu1);
        menu14.setCss("menu-icon fa fa-gear");
        menu14.setUrl("#");
        set.addAll(Arrays.asList(menu11, menu12, menu13, menu14));

        // 菜单管理 (系统默认菜单，不能改动)
        TreeEntity menu111 = new TreeEntity(TreeType.Menu, "菜单树", 0, false, menu11);
        menu111.setUrl("admin/ztree-type?treeType=Menu");
        menu111.setCss("menu-icon fa fa-caret-right");
        TreeEntity menu112 = new TreeEntity(TreeType.Menu, "部门树", 1, false, menu11);
        menu112.setUrl("admin/ztree-type?treeType=DepartMent");
        menu112.setCss("menu-icon fa fa-caret-right");
        TreeEntity menu113 = new TreeEntity(TreeType.Menu, "标准结构树", 2, false, menu11);
        menu113.setUrl("admin/ztree-type?treeType=Standard");
        menu113.setCss("menu-icon fa fa-caret-right");
        set.addAll(Arrays.asList(menu111, menu112, menu113));


        // 用户管理   (系统默认菜单，不能改动)
        TreeEntity menu131 = new TreeEntity(TreeType.Menu, "用户", 0, false, menu12);
        menu131.setUrl("admin/jqgrid-user");
        menu131.setCss("menu-icon fa fa-caret-right");
        TreeEntity menu132 = new TreeEntity(TreeType.Menu, "用户组", 1, false, menu12);
        menu132.setUrl("admin/jqgrid-group");
        menu132.setCss("menu-icon fa fa-caret-right");
        set.addAll(Arrays.asList(menu131, menu132));

        //角色管理   (系统默认菜单，不能改动)
        TreeEntity menu141 = new TreeEntity(TreeType.Menu, "菜单角色", 0, false, menu13);
        menu141.setUrl("admin/jqgrid-roles?treeType=Menu");
        menu141.setCss("menu-icon fa fa-caret-right");
        TreeEntity menu142 = new TreeEntity(TreeType.Menu, "标准资源角色", 1, false, menu13);
        menu142.setUrl("admin/jqgrid-roles?treeType=Standard");
        menu142.setCss("menu-icon fa fa-caret-right");
        set.addAll(Arrays.asList(menu141, menu142));

        //系统设置  (菜单示例，可根据需要修改)
        TreeEntity menu151 = new TreeEntity(TreeType.Menu, "参数配置", 0, false, menu14);
        menu151.setCss("menu-icon fa fa-caret-right");
        menu151.setUrl("");
        TreeEntity menu152 = new TreeEntity(TreeType.Menu, "定时任务", 1, false, menu14);
        menu152.setCss("menu-icon fa fa-caret-right");
        menu152.setUrl("");
        TreeEntity menu153 = new TreeEntity(TreeType.Menu, "系统日志", 2, false, menu14);
        menu153.setCss("menu-icon fa fa-caret-right");
        menu153.setUrl("");
        set.addAll(Arrays.asList(menu151, menu152, menu153));

        /**
         * 初始化 "业务系统管理"菜单，根据具体的应用修改。
         */
        //一级菜单
        TreeEntity menu2 = new TreeEntity(TreeType.Menu, "业务系统管理", 0, true, menu);
        menu2.setCss("menu-icon fa fa-desktop red bigger-130");
        menu2.setUrl("");
        set.addAll(Arrays.asList(menu2));

        //用户菜单 (菜单示例，可根据需要修改)
        TreeEntity menu21 = new TreeEntity(TreeType.Menu, "站点维护", 0, false, menu2);
        menu21.setCss("menu-icon fa fa-circle-o");
        menu21.setUrl("");
        TreeEntity menu22 = new TreeEntity(TreeType.Menu, "高级检索", 1, false, menu2);
        menu22.setCss("menu-icon fa fa-circle-o");
        menu22.setUrl("");
        TreeEntity menu23 = new TreeEntity(TreeType.Menu, "用户管理", 2, false, menu2);
        menu23.setCss("menu-icon fa fa-circle-o");
        menu23.setUrl("");
        TreeEntity menu24 = new TreeEntity(TreeType.Menu, "缴费管理", 3, false, menu2);
        menu24.setCss("menu-icon fa fa-circle-o");
        menu24.setUrl("");
        TreeEntity menu25 = new TreeEntity(TreeType.Menu, "模版设置", 4, false, menu2);
        menu25.setCss("menu-icon fa fa-circle-o");
        menu25.setUrl("");
        TreeEntity menu26 = new TreeEntity(TreeType.Menu, "消息管理", 5, false, menu2);
        menu26.setCss("menu-icon fa fa-circle-o");
        menu26.setUrl("");
        TreeEntity menu27 = new TreeEntity(TreeType.Menu, "个人设置", 6, false, menu2);
        menu27.setCss("menu-icon fa fa-circle-o");
        menu27.setUrl("");
        TreeEntity menu28 = new TreeEntity(TreeType.Menu, "其他菜单", 7, true, menu2);
        menu28.setCss("menu-icon fa fa-circle-o");
        menu28.setUrl("");
        set.addAll(Arrays.asList(menu21, menu22, menu23, menu24, menu25, menu26, menu27, menu28));


        // 其他菜单
        TreeEntity menu281 = new TreeEntity(TreeType.Menu, "菜单1", 0, false, menu28);
        menu281.setCss("menu-icon fa fa-caret-right");
        menu281.setUrl("");
        TreeEntity menu282 = new TreeEntity(TreeType.Menu, "菜单2", 1, false, menu28);
        menu282.setCss("menu-icon fa fa-caret-right");
        menu282.setUrl("");
        set.addAll(Arrays.asList(menu281, menu282));

        treeRepository.saveAll(set);
    }

    /**
     * 初始化一个超级用户 sysadmin ，作为系统保留用户，可以对系统做任何操作
     * -
     * 信息应该做修改
     * -
     * 如果没有更改，登录后提示，或强制修改
     */
    private void initUser() {

        /**
         * 用户已经初始化
         */
        if (userRepository.findByLoginName("admin").isPresent()) {
            log.info("user has initialized , continue");
            return;
        }

        log.info("initialize admin user ...");

        UserEntity user = new UserEntity();
        user.setLoginName("admin");
        user.setUserName("系统管理员");
        user.setPassword(BCryptPassWordUtils.encode("sysadmin"));
        user.setEmail("admin@mail.com");
        user.setValid(true);
        user.setEnabled(true);
        // user.setRoles(Sets.newHashSet(new RoleEntity("")));

        /**
         * 设置角色
         */
        Optional<RoleEntity> adminRole = roleRepository.findByName("系统管理员角色");
        if (!adminRole.isPresent()) {
            initRole();
            adminRole = roleRepository.findByName("系统管理员角色");  // 再次查询
        }

        if (adminRole.isPresent()) {
            user.addRole(adminRole.get());
        }

        /**
         * 设置组
         */
        Optional<GroupEntity> adminGroup = groupRepository.findByName("系统管理员组");
        if (!adminGroup.isPresent()) {
            initGroup();
            adminGroup = groupRepository.findByName("系统管理员组");// 再次查询
        }

        if (adminGroup.isPresent()) {
            user.addGroup(adminGroup.get());
        }

        userRepository.save(user);

    }


    /**
     * 初始化用户组 ，作为系统保留组
     * -
     */
    private void initGroup() {

        if (groupRepository.findByName("系统管理员组").isPresent() &&
                groupRepository.findByName("用户组").isPresent()) {

            log.info("group has initialized , continue");
            return;
        }

        log.info("initialize group ...");


        GroupEntity admins = new GroupEntity();
        admins.setName("系统管理员组");
        admins.setValid(true);
        admins.setEnabled(true);

        GroupEntity users = new GroupEntity();
        users.setName("用户组");
        users.setValid(true);
        users.setEnabled(true);

        groupRepository.save(admins);
        groupRepository.save(users);

    }

    /**
     * 初始化一个超级用户 sysadmin ，作为系统保留用户，可以对系统做任何操作
     * -
     */
    private void initRole() {

        if (roleRepository.findByName("系统管理员角色").isPresent() &&
                roleRepository.findByName("用户角色").isPresent()) {

            log.info("role has initialized , continue");
            return;
        }

        log.info("initialize role ...");


        RoleEntity admins = new RoleEntity();
        admins.setName("系统管理员角色");
        admins.setValid(true);
        admins.setEnabled(true);


        RoleEntity users = new RoleEntity();
        users.setName("用户角色");
        users.setValid(true);
        users.setEnabled(true);

        roleRepository.save(admins);
        roleRepository.save(users);
    }

}
