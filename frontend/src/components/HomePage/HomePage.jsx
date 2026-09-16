import HeroPanel from "../HeroPanel/HeroPanel.jsx";
import FeaturesSection from "../FeaturesSection/FeaturesSection.jsx";
import StatisticsSection from "../StatisticsSection/StatisticsSection.jsx";
import Footer from "../Footer/Footer.jsx";
import "./HomePage.css";

export default function HomePage({ onLogin, onRegister }) {
  return <div className="home-page"><HeroPanel onLogin={onLogin} onRegister={onRegister} /><FeaturesSection /><StatisticsSection /><Footer /></div>;
}
