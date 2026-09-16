import "./Footer.css";

const NAVIGATION = [["Home", "#home"], ["Games", "#features"], ["Features", "#features"], ["How to Play", "#statistics"], ["About", "#statistics"], ["Contact", "#footer"]];

export default function Footer() {
  return <footer className="site-footer" id="footer"><div className="site-footer__inner"><a className="site-footer__brand" href="#home" aria-label="Aceverse home"><span className="site-footer__crown" aria-hidden="true">♛</span><span>ACEVERSE<small>Play Beyond Limits</small></span></a><nav className="site-footer__nav" aria-label="Footer navigation">{NAVIGATION.map(([label, href]) => <a key={label} href={href}>{label}</a>)}</nav><div className="site-footer__socials" aria-label="Aceverse social links"><a href="#footer" aria-label="Aceverse community">Community</a><a href="#footer" aria-label="Aceverse updates">Updates</a></div></div><div className="site-footer__legal"><span>© Aceverse</span><span>Virtual coins only · No real-money gambling.</span></div></footer>;
}
