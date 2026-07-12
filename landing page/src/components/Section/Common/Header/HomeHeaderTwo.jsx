"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { FaAngleDown, FaAngleLeft } from "react-icons/fa";
import BrandLogo from "~/components/Ui/Logo/BrandLogo";
const HomeHeaderTwo = ({ logoSrc }) => {
  const [isActive, setIsActive] = useState(false);
  const [subMenuArray, setSubMenuArray] = useState([]);
  const [subMenuTextArray, setSubMenuTextArray] = useState([]);
  const [scrollClassName, setScrollClassName] = useState("");

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 100) {
        setScrollClassName("sticky-menu");
      } else {
        setScrollClassName("");
      }
    };

    window.addEventListener("scroll", handleScroll);

    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, []);

  const menuMainClickHandler = (e) => {
    if (typeof window !== "undefined" && window.innerWidth <= 991) {
      document.querySelectorAll(".nav-item").forEach(item => {
        item.classList.remove("active");
      });

      const hasChildren = e.target.closest(".nav-item-has-children");
      if (hasChildren) {
        e.preventDefault();
        if (hasChildren.classList.contains("nav-item-has-children")) {
          showSubMenu(hasChildren);
        }
      }
    }
  };

  const goBackClickHandler = () => {
    const lastItem = subMenuArray.slice(-1)[0];
    const lastItemText = subMenuTextArray.slice(-2)[0];
    setSubMenuArray(subMenuArray.slice(0, -1));
    setSubMenuTextArray(subMenuTextArray.slice(0, -1));
    if (lastItem) {
      if (subMenuArray.length >= 0) {
        if (
          !document
            .getElementById(lastItem)
            .classList.contains("nav-item-has-children")
        ) {
          document.getElementById(lastItem).style.animation =
            "slideRight 0.5s ease forwards";
          document.querySelector(".current-menu-title").innerHTML =
            lastItemText;
          setTimeout(() => {
            document.getElementById(lastItem).classList.remove("active");
          }, 300);
        } else {
          document.querySelector(".go-back").classList.remove("active");
        }
      }
    }
    if (subMenuArray.length === 1) {
      document.querySelector(".mobile-menu-head").classList.remove("active");
    }
  };

  const menuTriggerClickHandler = () => {
    toggleMenu();
  };

  const closeMenuClickHandler = () => {
    toggleMenu();
    const submenuAll = document.querySelectorAll(".sub-menu");
    submenuAll.forEach(submenu => {
      submenu.classList.remove("active");
      submenu.style.animation = "";
    });
  
    document.querySelector(".go-back").classList.remove("active");
  };

  const overlayClickHandler = () => {
    toggleMenu();
  };

  const toggleMenu = () => {
    setIsActive(!isActive);
    document.querySelector(".menu-overlay").classList.toggle("active");
  };

  const showSubMenu = (hasChildren) => {
    const submenuAll = document.querySelectorAll(".sub-menu");
    submenuAll.forEach((submenu) => submenu.classList.remove());
    const subMenu = hasChildren.querySelector(".sub-menu");
    setSubMenuArray([...subMenuArray, subMenu.id]);
    subMenu.classList.add("active");
    subMenu.style.animation = "slideLeft 0.5s ease forwards";
    const menuTitle = hasChildren.querySelector(".drop-trigger").textContent;
    setSubMenuTextArray([...subMenuTextArray, menuTitle]);
    document.querySelector(".current-menu-title").innerHTML = menuTitle;
    document.querySelector(".mobile-menu-head").classList.add("active");
  };

  useEffect(() => {
    const handleResize = () => {
      if (typeof window !== "undefined" && window.innerWidth > 991 && isActive) {
        toggleMenu();
      }
    };

    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, [isActive]);

  return (
    <header
      className={`site-header  zubuz-header-section bg-white ${scrollClassName}`}
      id="sticky-menu "
    >
      <div className="container">
        <nav className="navbar site-navbar">
          <BrandLogo imageSrc={logoSrc} />
          <div className="menu-block-wrapper">
            <div className="menu-overlay"></div>
            <nav
              className={`menu-block ${isActive ? "active" : ""}`}
              id="append-menu-header"
            >
              <div className="mobile-menu-head">
                <div className="go-back" onClick={goBackClickHandler}>
                  <FaAngleLeft />
                </div>
                <div className="current-menu-title"></div>
                <div
                  className="mobile-menu-close"
                  onClick={closeMenuClickHandler}
                >
                  {" "}
                  &times;
                </div>
              </div>
              <ul className="site-menu-main" onClick={menuMainClickHandler}>
                <li className="nav-item nav-item-has-children">
                  <Link href="" className="nav-link-item drop-trigger">
                    Demo{" "}
                    <i>
                      <FaAngleDown />
                    </i>
                  </Link>
                  <ul className="sub-menu" id="submenu-1">
                    <li className="sub-menu--item">
                      <Link href="/">
                        <span className="menu-item-text">Finance App</span>
                      </Link>
                    </li>
                    <li className="sub-menu--item">
                      <Link href="home-2">
                        <span className="menu-item-text">CRM Platform</span>
                      </Link>
                    </li>
                    <li className="sub-menu--item">
                      <Link href="home-3">
                        <span className="menu-item-text">SaaS Startups</span>
                      </Link>
                    </li>
                  </ul>
                </li>
                <li className="nav-item">
                  <Link href="/about-us" className="nav-link-item">
                    About Us
                  </Link>
                </li>
                <li className="nav-item nav-item-has-children">
                  <Link href="" className="nav-link-item drop-trigger">
                    Pages{" "}
                    <i>
                      <FaAngleDown />
                    </i>
                  </Link>
                  <ul className="sub-menu" id="submenu-2">
                    <li className="sub-menu--item">
                      <Link href="/about-us">
                        <span className="menu-item-text">About Us</span>
                      </Link>
                    </li>
                    <li className="sub-menu--item nav-item-has-children">
                      <Link href="" data-menu-get="h3" className="drop-trigger">
                        blog{" "}
                        <i>
                          <FaAngleDown />
                        </i>
                      </Link>
                      <ul className="sub-menu shape-none" id="submenu-3">
                        <li className="sub-menu--item">
                          <Link href="/blog">
                            <span className="menu-item-text">Our Blog</span>
                          </Link>
                        </li>
                        <li className="sub-menu--item">
                          <Link href="/blog-column-one">
                            <span className="menu-item-text">
                              blog column one
                            </span>
                          </Link>
                        </li>
                        <li className="sub-menu--item">
                          <Link href="/blog-column-two">
                            <span className="menu-item-text">
                              blog column two
                            </span>
                          </Link>
                        </li>
                        <li className="sub-menu--item">
                          <Link href="/single-blog">
                            <span className="menu-item-text">blog details</span>
                          </Link>
                        </li>
                      </ul>
                    </li>
                    <li class="sub-menu--item nav-item-has-children">
                      <Link href="" data-menu-get="h3" class="drop-trigger">
                        Service
                        <i>
                          <FaAngleDown />
                        </i>
                      </Link>
                      <ul class="sub-menu shape-none" id="submenu-4">
                        <li class="sub-menu--item">
                          <Link href="/service">
                            <span class="menu-item-text">service</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/single-service">
                            <span class="menu-item-text">service details</span>
                          </Link>
                        </li>
                      </ul>
                    </li>
                    <li class="sub-menu--item nav-item-has-children">
                      <Link href="" data-menu-get="h3" class="drop-trigger">
                        Team
                        <i>
                          <FaAngleDown />
                        </i>
                      </Link>
                      <ul class="sub-menu shape-none" id="submenu-5">
                        <li class="sub-menu--item">
                          <Link href="/team">
                            <span class="menu-item-text">team</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/single-team">
                            <span class="menu-item-text">team details</span>
                          </Link>
                        </li>
                      </ul>
                    </li>
                    <li class="sub-menu--item nav-item-has-children">
                      <Link href="" data-menu-get="h3" class="drop-trigger">
                        Career
                        <i>
                          <FaAngleDown />
                        </i>
                      </Link>
                      <ul class="sub-menu shape-none" id="submenu-6">
                        <li class="sub-menu--item">
                          <Link href="/career">
                            <span class="menu-item-text">Career</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/single-career">
                            <span class="menu-item-text">Career details</span>
                          </Link>
                        </li>
                      </ul>
                    </li>
                    <li class="sub-menu--item nav-item-has-children">
                      <Link href="" data-menu-get="h3" class="drop-trigger">
                        Portfolio
                        <i>
                          <FaAngleDown />
                        </i>
                      </Link>
                      <ul class="sub-menu shape-none" id="submenu-7">
                        <li class="sub-menu--item">
                          <Link href="/portfolio-grid">
                            <span class="menu-item-text">portfolio gird</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/portfolio-classic">
                            <span class="menu-item-text">
                              portfolio classic
                            </span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/single-portfolio">
                            <span class="menu-item-text">Single Portfolio</span>
                          </Link>
                        </li>
                      </ul>
                    </li>
                    <li class="sub-menu--item nav-item-has-children">
                      <Link href="" data-menu-get="h3" class="drop-trigger">
                        Utility
                        <i>
                          <FaAngleDown />
                        </i>
                      </Link>
                      <ul class="sub-menu shape-none" id="submenu-8">
                        <li class="sub-menu--item">
                          <Link href="/faq">
                            <span class="menu-item-text">faq</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/errors-404">
                            <span class="menu-item-text">Error 404</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/integrations">
                            <span class="menu-item-text">integrations</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/testimonials">
                            <span class="menu-item-text">testimonials</span>
                          </Link>
                        </li>
                        <li class="sub-menu--item">
                          <Link href="/coming-soon">
                            <span class="menu-item-text">Coming Soon</span>
                          </Link>
                        </li>
                      </ul>
                    </li>
                  </ul>
                </li>
                <li className="nav-item nav-item-has-children">
                  <Link href="" className="nav-link-item drop-trigger">
                    Blog{" "}
                    <i>
                      <FaAngleDown />
                    </i>
                  </Link>
                  <ul className="sub-menu" id="submenu-10">
                    <li className="sub-menu--item">
                      <Link href="/blog">
                        <span className="menu-item-text">Our Blog</span>
                      </Link>
                    </li>
                    <li className="sub-menu--item">
                      <Link href="/blog-column-one">
                        <span className="menu-item-text">blog column one</span>
                      </Link>
                    </li>
                    <li className="sub-menu--item">
                      <Link href="/blog-column-two">
                        <span className="menu-item-text">blog column two</span>
                      </Link>
                    </li>
                    <li className="sub-menu--item">
                      <Link href="/single-blog">
                        <span className="menu-item-text">blog details</span>
                      </Link>
                    </li>
                  </ul>
                </li>
                <li className="nav-item">
                  <Link href="/contact-us" className="nav-link-item">
                    Contact Us
                  </Link>
                </li>
              </ul>
            </nav>
          </div>

          <div className="header-btn header-btn-l1 ms-auto d-none d-xs-inline-flex">
            <div className="zubuz-header-btn-wrap">
              <Link className="zubuz-login-btn" href="sign-in">
                Login
              </Link>
            </div>
            <Link
              className="zubuz-default-btn zubuz-header-btn pill"
              href="contact-us"
            >
              <span>Get Started</span>
            </Link>
          </div>
          <div
            className="mobile-menu-trigger light"
            onClick={menuTriggerClickHandler}
          >
            <span></span>
          </div>
        </nav>
      </div>
    </header>
  );
};

export default HomeHeaderTwo;
